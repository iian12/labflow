from typing import Any

import requests

from config import LabFlowConfig

class HttpClient:

    def __init__(self, config: LabFlowConfig):
        self._config = config
        self.session = requests.Session()

        self.session.headers.update({
            "Aurhotization": (
                f"Bearer {config.api_key}"
            ),
            "Content-Type": "application/json",
            "User-Agent": "labflow-python-sdk"
        })

    def post(self, path: str, payload: dict[str, Any]) -> dict[str, Any]:
        url = (
            f"{self._config.base_url.rstrip('/')}"
            f"{path}"
        )

        response = self._session.post(
            url,
            json=payload,
            timeout=self._config.timeout_seconds,
        )
        
        response.raise_for_status()

        if not response.content:
            return {}

        return response.json()
