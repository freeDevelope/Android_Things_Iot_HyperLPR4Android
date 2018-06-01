# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(OpenCV_DIR D:/opencv-3.3.0-android-sdk/OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)

include_directories(src/main/cpp)
include_directories(src/main/cpp/src)
include_directories(src/main/cpp/include)




add_library( # Sets the name of the library.
                hyperlpr
             # Sets the library as a shared library.
             SHARED
             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.

             src/main/cpp/src/CNNRecognizer.cpp
             src/main/cpp/src/FastDeskew.cpp
             src/main/cpp/src/FineMapping.cpp
             src/main/cpp/src/Pipeline.cpp
             src/main/cpp/src/PlateDetection.cpp
             src/main/cpp/src/PlateSegmentation.cpp
             src/main/cpp/src/Recognizer.cpp
             src/main/cpp/src/SegmentationFreeRecognizer.cpp
             src/main/cpp/javaWarpper.cpp

             )


target_link_libraries( # Specifies the target library.
                       hyperlpr
                       ${OpenCV_LIBS}
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )