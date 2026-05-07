import os
from langchain_text_splitters import RecursiveCharacterTextSplitter
from pathlib import Path
import numpy as np
from sentence_transformers import SentenceTransformer
import chromadb
import uuid
from typing import List, Dict, Any
import requests

# --- Document Processing ---
def process_all_txts(txt_directory):
    all_documents = []
    txt_dir = Path(txt_directory)
    txt_files = list(txt_dir.glob("**/*.txt"))
    for txt_file in txt_files:
        try:
            with open(txt_file, "r", encoding="utf-8") as f:
                content = f.read()
                doc = {
                    "content": content,
                    "metadata": {
                        "source_file": txt_file.name,
                        "file_type": "txt"
                    }
                }
                all_documents.append(doc)
        except Exception:
            continue
    return all_documents

# --- Text Splitting ---
def split_documents(documents, chunk_size=1000, chunk_overlap=200):
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap,
        length_function=len,
        separators=["\n\n", "\n", " ", ""]
    )
    from langchain_core.documents import Document
    doc_objs = [Document(page_content=doc['content'], metadata=doc['metadata']) for doc in documents]
    split_docs = text_splitter.split_documents(doc_objs)
    return split_docs

# --- Embedding Manager ---
class EmbeddingManager:
    def __init__(self, model_name: str = "all-MiniLM-L6-v2"):
        self.model_name = model_name
        self.model = None
        self._load_model()
    def _load_model(self):
        self.model = SentenceTransformer(self.model_name)
    def generate_embeddings(self, texts: List[str]) -> np.ndarray:
        if not self.model:
            raise ValueError("Model not loaded")
        embeddings = self.model.encode(texts, show_progress_bar=False)
        return embeddings

# --- Vector Store ---
# --- Vector Store ---
class VectorStore:
    def __init__(self, collection_name: str = "all_text_documents", persist_directory: str = "./data/vector_store"):
        self.collection_name = collection_name
        self.persist_directory = persist_directory
        self.client = None
        self.collection = None
        self._initialize_store()
    def _initialize_store(self):
        os.makedirs(self.persist_directory, exist_ok=True)
        self.client = chromadb.PersistentClient(path=self.persist_directory)
        self.collection = self.client.get_or_create_collection(
            name=self.collection_name,
            metadata={"description": "Text document embeddings for RAG"}
        )
    def add_documents(self, documents: List[Any], embeddings: np.ndarray):
        if len(documents) != len(embeddings):
            raise ValueError("Number of documents must match number of embeddings")
        ids = []
        metadatas = []
        documents_text = []
        embeddings_list = []
        for i, (doc, embedding) in enumerate(zip(documents, embeddings)):
            doc_id = f"doc_{uuid.uuid4().hex[:8]}_{i}"
            ids.append(doc_id)
            metadata = dict(doc.metadata)
            metadata['doc_index'] = i
            metadata['content_length'] = len(doc.page_content)
            metadatas.append(metadata)
            documents_text.append(doc.page_content)
            embeddings_list.append(embedding.tolist())
        self.collection.add(
            ids=ids,
            embeddings=embeddings_list,
            metadatas=metadatas,
            documents=documents_text
        )
 
# --- RAG Retriever ---
class RAGRetriever:
    def __init__(self, vector_store: VectorStore, embedding_manager: EmbeddingManager):
        self.vector_store = vector_store
        self.embedding_manager = embedding_manager
    def retrieve(self, query: str, top_k: int = 5, score_threshold: float = 0.0) -> List[Dict[str, Any]]:
        query_embedding = self.embedding_manager.generate_embeddings([query])[0]
        results = self.vector_store.collection.query(
            query_embeddings=[query_embedding.tolist()],
            n_results=top_k
        )
        retrieved_docs = []
        if results['documents'] and results['documents'][0]:
            documents = results['documents'][0]
            metadatas = results['metadatas'][0]
            distances = results['distances'][0]
            ids = results['ids'][0]
            for i, (doc_id, document, metadata, distance) in enumerate(zip(ids, documents, metadatas, distances)):
                similarity_score = 1 - distance
                if similarity_score >= score_threshold:
                    retrieved_docs.append({
                        'id': doc_id,
                        'content': document,
                        'metadata': metadata,
                        'similarity_score': similarity_score,
                        'distance': distance,
                        'rank': i + 1
                    })
        return retrieved_docs

# --- Ollama RAG Response ---
def ollama_rag_response(
    query,
    rag_retriever,
    ollama_url="http://localhost:11434/api/generate",
    model_name="qwen3.5:2b",
    top_k=5
):
    # Basic greetings and responses
    user_input = query.strip().lower()
    greetings = ["hi", "hello", "hey", "good morning", "good afternoon", "good evening"]
    if user_input in greetings:
        return "Hello! How can I assist you today?"
    if user_input in ["how are you", "how are you?", "how are you doing?"]:
        return "I'm an AI assistant, always ready to help! How can I assist you?"
    if user_input in ["who are you", "who are you?", "what are you?"]:
        return "I'm your digital assistant chatbot. Ask me anything about the platform!"
    if user_input in ["thank you", "thanks", "thanks!"]:
        return "You're welcome! If you have more questions, just ask."
    if user_input in ["bye", "goodbye", "see you", "see you later"]:
        return "Goodbye! Have a great day!"
    # Default RAG retrieval
    retrieved_docs = rag_retriever.retrieve(query, top_k=top_k)
    context = "\n\n".join([doc['content'] for doc in retrieved_docs]) if retrieved_docs else ""
    if not context:
        return "No relevant context found to answer the question."
    prompt = f"""Context:\n{context}\n\nQuestion: {query}\nAnswer:"""
    payload = {
        "model": model_name,
        "prompt": prompt
    }
    response = requests.post(ollama_url, json=payload, stream=True)
    response.raise_for_status()
    answer = ""
    for line in response.iter_lines():
        if line:
            try:
                import json
                data = json.loads(line.decode("utf-8"))
                answer += data.get("response", "")
            except Exception:
                continue
    return answer if answer else "No response from LLM."

# --- Initialize pipeline objects at module level ---
# (You may want to adjust the data path as needed)
all_txt_documents = process_all_txts("./data/textfiles")
chunks = split_documents(all_txt_documents)
embedding_manager = EmbeddingManager()
vectorstore = VectorStore(persist_directory="./data/vector_store")
# Only add if collection is empty and there is data to add (avoid duplicates and empty input)
if vectorstore.collection.count() == 0 and len(chunks) > 0:
    texts = [chunk.page_content for chunk in chunks]
    embeddings = embedding_manager.generate_embeddings(texts)
    if len(embeddings) > 0:
        vectorstore.add_documents(chunks, embeddings)
rag_retriever = RAGRetriever(vectorstore, embedding_manager)
