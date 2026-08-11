from dataclasses import dataclass

@dataclass(frozen=True)
class LabFlowConfig:
    # 프로젝트 API Key
    api_key: str

    # Backend URL
    base_url: str = "http://localhost:8080"

    # HTTP 요청 timeout
    timeout_seconds: float = 10.0
