package com.example.cameraobjectanalyzer.utils

object Constants {
    const val MODEL_PATH = "yolov8n_float16.tflite"

    // YOLOv8 input size
    const val MODEL_INPUT_SIZE = 640

    // Confidence threshold
    const val CONFIDENCE_THRESHOLD = 0.5f

    // Non-maximum suppression threshold
    const val NMS_THRESHOLD = 0.45f

    // COCO Dataset classes (80 classes)
    val COCO_CLASSES = listOf(
        "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
        "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
        "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
        "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
        "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
        "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
        "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
        "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
        "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator",
        "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"
    )

    const val UiDebugTag = "UI_DEBUG_TAG"
    const val PerformanceDebugTag = "PERFORMANCE_DEBUG_TAG"
    const val ImageDebugTag = "IMAGE_DEBUG_TAG"
    const val TFModelDebugTag = "TF_MODEL_DEBUG_TAG"
    const val BASE_URL = "http://192.168.2.119:5000"
    const val NetworkDebugTag = "NETWORK_DEBUG_TAG"
    const val CrashDebugTag = "CRASH_DEBUG_TAG"



}