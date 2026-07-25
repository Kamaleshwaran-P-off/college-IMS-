from __future__ import annotations

import numpy as np
from fastapi import FastAPI
from pydantic import BaseModel, Field
from sklearn.linear_model import LogisticRegression

app = FastAPI(title="Risk Prediction Service")


class RiskRequest(BaseModel):
    marks: list[float] = Field(default_factory=list)
    attendance: float = Field(..., ge=0, le=100)
    assignment_completion: float = Field(..., ge=0, le=100)


class RiskResponse(BaseModel):
    risk: str
    score: float


def _generate_training_data(n: int = 1200) -> tuple[np.ndarray, np.ndarray]:
    rng = np.random.default_rng(42)
    marks = rng.uniform(30, 95, size=n)
    attendance = rng.uniform(40, 100, size=n)
    completion = rng.uniform(30, 100, size=n)

    X = np.column_stack([marks, attendance, completion])

    # Risk heuristic
    risk_score = (100 - marks) * 0.4 + (100 - attendance) * 0.35 + (100 - completion) * 0.25
    y = np.zeros(n, dtype=int)
    y[risk_score >= 55] = 2  # HIGH
    y[(risk_score >= 35) & (risk_score < 55)] = 1  # MEDIUM
    return X, y


X_train, y_train = _generate_training_data()
_model = LogisticRegression(max_iter=500, multi_class="multinomial")
_model.fit(X_train, y_train)


def _predict_risk(avg_marks: float, attendance: float, completion: float) -> tuple[str, float]:
    X = np.array([[avg_marks, attendance, completion]])
    probs = _model.predict_proba(X)[0]
    idx = int(np.argmax(probs))
    label = "LOW" if idx == 0 else "MEDIUM" if idx == 1 else "HIGH"
    score = float(probs[idx])
    return label, score


@app.post("/predict-risk", response_model=RiskResponse)
async def predict_risk(payload: RiskRequest):
    avg_marks = float(np.mean(payload.marks)) if payload.marks else 60.0
    label, score = _predict_risk(avg_marks, payload.attendance, payload.assignment_completion)
    return RiskResponse(risk=label, score=score)
