from pathlib import Path
from typing import Any

import requests

from labflow.config import LabFlowConfig


class HttpClient:

    def __init__(self, config: LabFlowConfig):
        self._config = config
        self._session = requests.Session()

        self._session.headers.update({
            "Authorization": f"Bearer {config.api_key}",
            "User-Agent": "labflow-python-sdk/0.1.0",
        })

    def post_json(
        self,
        path: str,
        payload: dict[str, Any],
    ) -> dict[str, Any]:

        response = self._session.post(
            self._url(path),
            json=payload,
            timeout=self._config.timeout_seconds,
        )

        response.raise_for_status()

        if not response.content:
            return {}

        return response.json()

    def post_file(
        self,
        path: str,
        file_path: str | Path,
        data: dict[str, str],
    ) -> dict[str, Any]:

        path_object = Path(file_path)

        with path_object.open("rb") as file:
            response = self._session.post(
                self._url(path),
                files={
                    "file": (
                        path_object.name,
                        file,
                    )
                },
                data=data,
                timeout=self._config.timeout_seconds,
            )

        response.raise_for_status()

        if not response.content:
            return {}

        return response.json()

    def _url(self, path: str) -> str:
        return (
            f"{self._config.base_url.rstrip('/')}"
            f"/{path.lstrip('/')}"
        )

    def close(self) -> None:
        self._session.close()