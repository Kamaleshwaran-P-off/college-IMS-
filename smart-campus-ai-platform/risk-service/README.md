# Risk Prediction Microservice

## Setup

```bash
cd C:\Users\DREAMZ\OneDrive\Desktop\lmsss codec\smart-campus-ai-platform\risk-service
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 8001
```

## API

`POST /predict-risk`

```json
{
  "marks": [65, 72, 58],
  "attendance": 82,
  "assignment_completion": 75
}
```

Response:
```json
{
  "risk": "MEDIUM",
  "score": 0.63
}
```
