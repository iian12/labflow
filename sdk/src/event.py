from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any


@dataclass(frozen=True)
class MetricEvent:
    # 사용자가 관리하는 epoch
    epoch: int

    # Run 전체에서 증가하는 global step
    step: int

    # Run 시작 후 경과 시간 (seconds)
    elapsed_time: float

    # 실제 Metric 값
    metrics: dict[str, str]

    # SDK가 자동 수집한 시스템 정보
    system: dict[str, Any]

    # Metric 기록 시작 시각
    recorded_at: datetime

    def to_payload(self) -> dict[str, Any]:
        return {
            "epoch": self.epoch,
            "step": self.step,
            "elapsedTime": self.elapsed_time,
            "metrics": self.metrics,
            "system": self.system,
            "recordedAt": self.recorded_at,
        }

    @staticmethod
    def now_utc() -> datetime:
        return datetime.now(timezone.utc)