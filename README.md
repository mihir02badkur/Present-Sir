# Present-Sir
## About the App :

This is an Attendance Tracking system using Real-time facial recognition. This app allows users to mark their attendance for any subject just by capturing 
their image using their phone's camera.

The app consists of a simple email password authentication system and takes your image when you sign up for the first time to verify your face. The next 
time you log in, you can choose the subject you want, capture your image, and the app verifies if the face is yours. If your face matches, your attendance 
for that subject gets marked; else, it shows you that your face didn't match. Once your attendance is marked, you cannot mark it again for the same day. 
The app also keeps a record of your attendance for every month and each subject for a year.

It also has a faculty screen (the faculty can only be added from the backend) that shows each student's data, and the teacher can know the attendance for a
particular month and particular subject for any desired student.

## Functionality & Concepts used :

The App has a very simple and interactive interface that helps the students mark their attendance and keeps the administration informed. 
Following are a few android concepts used to achieve the functionalities in App:
- **Simple & Easy Views Design**: The App provide an intuitive and easy to use UI consisting of cards with interactive buttons, making it easier for students to 
track their attendance record.
- **Firebase Firestore**: Student data, including their face (for recognition), are backed up online using Firebase Cloud Firestore.
- **Email Password Authentication**: Student and faculty administration are being authenticated using Firebase Email-Password Authentication, where faculty can be 
added from the Firebase only.
- **RecyclerView**: To present the list of all students and display their attendance records to faculty, an efficient RecyclerView is used.
- **DrawerLayout**: The main activity uses a DrawerLayout to smoothly switch between various activities.
- **Google ML Kit**: It is used to detect face from a given image which is then fed to the TensorFlow Lite model.
- **TensorFlow Lite**:  The Deep Neural Network-based model, with a high accuracy rate, compares faces and tells if they are of the same person or not.

## Screenshots from App : 

<img width = "559" alt = "sampleImage" src = "https://user-images.githubusercontent.com/84968175/170837736-0b0b5138-2a3d-4415-9e1e-ae7e1cd01e9d.jpg"/>


## Real Time Face Recognition :

One of the main features of this app is Real-Time Face Recognition which is achieved using a Deep Neural network-based TensorFlow lite model.
The working of the Facial Recognition system is as follows- 
1. Using Google ML Kit on an input image, the face is detected.
2. The image is warped using the detected landmarks to align the face (so that all cropped faces have the eyes in the same position).
3. Next, the face is cropped and resized adequately along with some image pre-processing operations like normalizing and whitening the face to feed the 
recognition Deep Learning model. 
4. The deep neural network DNN takes as input a face F and gives as output a D =128 dimensions vector (of floats). This vector E is known as embeddings. 
These embeddings are created such as the similarity between the two faces F1 and F2 can be computed simply as the euclidean distance between the embeddings
E1 and E2.
5. Then, the two faces, F1 and F2, are compared by computing their Similarity, and checked against some threshold. If lower, it says that both faces are
from the same person.
Although the model used is heavy, its high accuracy and great speed are tempting to try using it.

## Additional Information :
Credentials of Faculty account for testing -
- Email - presentsirfaculty12@gmail.com
- Password - Presentsir@123 (Case-sensitive)

### Application Link :
[APK LINK HERE](https://github.com/RainaJain5/Present-Sir/blob/main/Present%20Sir.apk) 
