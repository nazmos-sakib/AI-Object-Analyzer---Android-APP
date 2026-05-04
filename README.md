# AI-Object-Analyzer---Android-APP

======================================
ImageProxy (YUV)
   ↓
Bitmap
   ↓
JPEG bytes
   ↓ (HTTP POST)
Flask server
   ↓
PIL / OpenCV image
   ↓
YOLO inference
   ↓
JSON response
   ↓
Android app
=====================================
CameraX (ImageProxy)
   ↓
PreviewView (UI - smooth)
   ↓
Background thread (every N frames)
   ↓
Send JPEG → server (HTTP/WebSocket)
   ↓
YOLO inference
   ↓
Return bounding boxes (JSON)
   ↓
Draw overlay on PreviewView

=====================================

Android CameraX
↓ (JPEG frame every 100–300ms)
HTTP POST /infer
↓
Flask + YOLOv8
↓
JSON bounding boxes
↓
Android overlay rendering