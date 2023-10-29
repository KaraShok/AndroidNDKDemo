//
// Created by KaraShokZ on 2023/3/23.
//

#include "opencv2/opencv.hpp"

#ifndef NDKDEMO_CASCADEDETECTORADAPTER_H
#define NDKDEMO_CASCADEDETECTORADAPTER_H

class CascadeDetectorAdapter : public cv::DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {

        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects) {

        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize,
                                   maxObjSize);

    }

    virtual ~CascadeDetectorAdapter() {

    }

private:
    CascadeDetectorAdapter();

    cv::Ptr<cv::CascadeClassifier> Detector;
};

#endif //NDKDEMO_CASCADEDETECTORADAPTER_H
