from dataclasses import dataclass

@dataclass(frozen=True)
class LabFlowConfig:

    # 프로젝트 API Key
    api_key: str

    # LabFlow Backend 주소
    base_url: str = "http://localhost:8080"

    # HTTP timeout
    timeout_seconds: float = 5.0

    # System Metric 수집 주기
    system_metric_interval_seconds: float = 1.0

    # stdout / stderr 자동 수집 여부
    capture_output: bool = True