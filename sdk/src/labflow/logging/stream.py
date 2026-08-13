import threading
import time
from datetime import datetime, timezone
from typing import TextIO

from labflow.api.http_client import HttpClient


class LabFlowStream:

    def __init__(
        self,
        *,
        original: TextIO,
        stream_name: str,
        run_id: int,
        http_client: HttpClient,
        started_at: float,
    ):
        self._original = original
        self._stream_name = stream_name
        self._run_id = run_id
        self._http_client = http_client
        self._started_at = started_at

        self._buffer = ""
        self._sequence = 0
        self._lock = threading.Lock()

    def write(self, text: str) -> int:

        # 기존 터미널 출력 유지
        written = self._original.write(text)
        self._original.flush()

        if not text:
            return written

        with self._lock:
            self._buffer += text

            while "\n" in self._buffer:
                line, self._buffer = (
                    self._buffer.split(
                        "\n",
                        1,
                    )
                )

                if line.strip():
                    self._send(line)

        return written

    def flush(self) -> None:

        self._original.flush()

        with self._lock:

            if self._buffer.strip():
                self._send(self._buffer)

            self._buffer = ""

    def _send(self, message: str) -> None:

        self._sequence += 1

        payload = {
            "sequence": self._sequence,
            "stream": self._stream_name,
            "message": message,
            "elapsedTime": (
                time.monotonic()
                - self._started_at
            ),
            "recordedAt": (
                datetime.now(timezone.utc)
                .isoformat()
            ),
        }

        try:
            self._http_client.post_json(
                (
                    f"/api/v1/sdk/runs/"
                    f"{self._run_id}/logs"
                ),
                payload,
            )

        except Exception:
            # 로그 저장 실패 때문에
            # 학습 프로그램이 죽으면 안 됨
            pass

    def isatty(self) -> bool:
        return self._original.isatty()

    def fileno(self) -> int:
        return self._original.fileno()

    @property
    def encoding(self):
        return self._original.encoding