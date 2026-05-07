
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
import uvicorn

# Import your classes and functions from your notebook/module
from rag_embedding import rag_retriever, ollama_rag_response


app = FastAPI()

# Enable CORS for all origins (for development)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class QueryRequest(BaseModel):
    question: str
    model_name: Optional[str] = "qwen3.5:2b"
    top_k: Optional[int] = 5

@app.post("/ask")
def ask_question(request: QueryRequest):
    try:
        answer = ollama_rag_response(
            request.question,
            rag_retriever,
            model_name=request.model_name,
            top_k=request.top_k
        )
        return {"answer": answer}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run("rag_api:app", host="0.0.0.0", port=8000, reload=True)
