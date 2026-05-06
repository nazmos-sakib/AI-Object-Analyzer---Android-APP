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


original → resized → padded (letterbox) → 1280x1280

Input image: 720 x 1280

Scale factor = 1280 / 1280 = 1.0
New size = 720 x 1280
Padding:
top/bottom = (1280 - 720)/2 = 280 px
previewView = width:1080 height: 2294