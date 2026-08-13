from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any


@dataclass(frozen=True)
class MetricEvent:

    # 사용자가 전달한 Epoch
    epoch: int | None

    # 사용자가 전달한 Step
    step: int | None

    # Run 시작 이후 경과 시간
    elapsed_time: float

    # 사용자가 자유롭게 전달하는 Metric
    metrics: dict[str, str]

    # Metric 측정 시각
    recorded_at: datetime

    def to_payload(self) -> dict[str, Any]:
        return {
            "epoch": self.epoch,
            "step": self.step,
            "elapsedTime": self.elapsed_time,
            "metrics": self.metrics,
            "recordedAt": self.recorded_at.isoformat(),
        }

    @staticmethod
    def now_utc() -> datetime:
        return datetime.now(timezone.utc)