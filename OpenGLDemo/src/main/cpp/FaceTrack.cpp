//
// Created by KaraShokZ on 2023/5/20.
//

#include "FaceTrack.h"

FaceTrack::FaceTrack(const char *model, const char *seeta) {
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(makePtr<CascadeClassifier>(model));

    Ptr<CascadeDetectorAdapter> trackDetector = makePtr<CascadeDetectorAdapter>(makePtr<CascadeClassifier>(model));

    DetectionBasedTracker::Parameters trackParams;

    tracker = makePtr<DetectionBasedTracker>(mainDetector,trackDetector,trackParams);

    faceAlignment = makePtr<seeta::FaceAlignment>(seeta);
}

void FaceTrack::startTracking() {
    tracker->run();
}

void FaceTrack::stopTracking() {
    tracker->stop();
}

void FaceTrack::detector(Mat src, vector<Rect2f> &rects) {
    vector<Rect> faces;
    tracker->process(src);
    tracker->getObjects(faces);
    if (faces.size()) {
        Rect face = faces[0];
        rects.push_back(Rect2f(face.x,face.y,face.width,face.height));

        // 关键点定位，保存 5 个关键点的坐标
        // 0：左眼；1：右眼；2：鼻头；3：嘴左；4：嘴右
        seeta::FacialLandmark points[5];

        // 图像数据
        seeta::ImageData imageData(src.cols,src.rows);
        imageData.data = src.data;

        // 指定人脸部位
        seeta::FaceInfo faceInfo;
        seeta::Rect bbox;
        bbox.x = face.x;
        bbox.y = face.y;
        bbox.width = face.width;
        bbox.height = face.height;
        faceInfo.bbox = bbox;
        faceAlignment->PointDetectLandmarks(imageData,faceInfo,points);

        for (int i = 0; i < 5; ++i) {
            rects.push_back(Rect2f(points[i].x,points[i].y,0,0));
        }
    }
}