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

=====================================
original → resized → padded (letterbox) → 1280x1280

Input image: 720 x 1280

Scale factor = 1280 / 1280 = 1.0
New size = 720 x 1280
Padding:
top/bottom = (1280 - 720)/2 = 280 px
previewView = width:1080 height: 2294

=====================================
PreviewView (live camera)
   +
Server rendered image overlay

Meaning:

> keep camera preview ALWAYS visible
> draw server-rendered image ON TOP of preview
> update overlay whenever server sends a new image

This gives:

> smooth live camera feeling
> server image updates independently
> no black screen
> better perceived FPS

1. Show PreviewView initially
2. Send frames continuously
3. When first server image arrives:
   hide PreviewView
4. Continue displaying only server images