@echo off
REM Start the DEMP Python chatbot service on port 8001
cd /d "%~dp0"
if not exist .venv (
    python -m venv .venv
)
call .venv\Scripts\activate.bat
pip install -q -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 8001 --reload
