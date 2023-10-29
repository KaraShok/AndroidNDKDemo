//
// Created by KaraShokZ on 2023/5/20.
//

#ifndef NDKDEMO_FACETRACK_H
#define NDKDEMO_FACETRACK_H

#include "include/opencv2/opencv.hpp"
#include "include/opencv2/objdetect.hpp"
#include "FaceAlignment/include/face_alignment.h"
#include <vector>
#include "include/CascadeDetectorAdapter.h"

using namespace std;
using namespace cv;

class FaceTrack {
public:
    FaceTrack(const char *model, const char *seeta);

    void detector(Mat src, vector<Rect2f> &rects);

    void startTracking();

    void stopTracking();

private:
    Ptr<DetectionBasedTracker> tracker;
    Ptr<seeta::FaceAlignment> faceAlignment;
};
#endif //NDKDEMO_FACETRACK_H