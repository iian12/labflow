"""Real HTTP transport checks; no running backend or API key is required."""
import tempfile
import threading
import unittest
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

import requests
from labflow.api.http_client import HttpClient
from labflow.config import LabFlowConfig


class ApiKeyTransportTest(unittest.TestCase):
    def setUp(self):
        self.received = []
        received = self.received

        class Handler(BaseHTTPRequestHandler):
            def do_POST(self):
                body = self.rfile.read(int(self.headers.get("Content-Length", 0)))
                received.append((self.path, self.headers.get("Authorization"),
                                 self.headers.get("Content-Type"), body))
                if self.headers.get("Authorization") != "Bearer lfp_test_secret":
                    self.send_response(401)
                    self.end_headers()
                    self.wfile.write(b'{"code":"UNAUTHORIZED"}')
                else:
                    self.send_response(204)
                    self.end_headers()

            def log_message(self, *args):
                pass

        self.server = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)
        self.thread.start()
        self.client = HttpClient(LabFlowConfig(
            api_key="lfp_test_secret", base_url=f"http://127.0.0.1:{self.server.server_port}"))

    def tearDown(self):
        self.client.close()
        self.server.shutdown()
        self.server.server_close()
        self.thread.join()

    def test_every_json_request_and_multipart_include_bearer_key(self):
        suffixes = ["", "/1/parameters", "/1/metrics", "/1/system-metrics",
                    "/1/logs", "/1/complete", "/1/fail"]
        for suffix in suffixes:
            self.assertEqual({}, self.client.post_json("/api/v1/sdk/runs" + suffix, {}))
        with tempfile.TemporaryDirectory() as directory:
            artifact = Path(directory) / "model.txt"
            artifact.write_text("test artifact")
            self.assertEqual({}, self.client.post_file("/api/v1/sdk/runs/1/artifacts", artifact,
                                                      {"type": "model", "name": "test"}))
        self.assertEqual(8, len(self.received))
        self.assertTrue(all(row[1] == "Bearer lfp_test_secret" for row in self.received))
        self.assertTrue(self.received[-1][2].startswith("multipart/form-data; boundary="))
        self.assertIn(b"test artifact", self.received[-1][3])

    def test_invalid_key_raises_http_error_for_json_and_multipart(self):
        self.client.close()
        self.client = HttpClient(LabFlowConfig(
            api_key="wrong", base_url=f"http://127.0.0.1:{self.server.server_port}"))
        with self.assertRaises(requests.HTTPError) as error:
            self.client.post_json("/api/v1/sdk/runs", {})
        self.assertEqual(401, error.exception.response.status_code)
        with tempfile.TemporaryDirectory() as directory:
            artifact = Path(directory) / "model.txt"
            artifact.write_text("test")
            with self.assertRaises(requests.HTTPError) as error:
                self.client.post_file("/api/v1/sdk/runs/1/artifacts", artifact, {})
            self.assertEqual(401, error.exception.response.status_code)


if __name__ == "__main__":
    unittest.main()
