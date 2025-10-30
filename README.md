# 끼리끼리


> 
> 개발 기간 : 2025.08.06 ~ 2025.09.13
> 

## 🌟 개발단계

> 요구사항 명세서                                                 
> https://docs.google.com/spreadsheets/d/1x4YWIlUO0vx7K2CE0nvun3ryrkuAntxt/edit?gid=1388090367#gid=1388090367
>
> 피그마
> https://www.figma.com/design/tRQobtFSTbqcQ2u8kLvNaI/%EC%95%B1%EC%8A%A4%EC%BF%A8_3%EA%B8%B0_%ED%8C%8C%EC%9D%B4%EB%84%90%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8_1%ED%8C%80?node-id=846-4449&t=30lCzx21T06wlP7n-0
 
## 🔍 프로젝트 소개

> **"친한 친구들과 편하게 소통하고 싶지 않나요?"**
> 
> 
> 끼리끼리는 소중한 사람들과 비공개 그룹을 만들어 가볍고 의미 있는 대화를 나누는 일상 공유 플랫폼입니다.
> 
> 기존 SNS는 다수와의 소통을 강조해 불필요한 관계와 정보 피로감을 초래합니다. 이를 해결하기 위해, 우리는 제한된 관계와 시간 속에서 깊이 있는 대화를 유도하는 SNS를 기획했습니다.
> 
> 끼리끼리에서는 그룹원들끼리 실시간으로 요청하고 응답하며, 30분 제한을 통해 즉각적인 소통을 유도합니다. 또한, 하루 한 개의 질문을 제공해 더욱 능동적인 대화를 나눌 수 있도록 설계했습니다.
>
> 

## 🐈기술 스택

### **Environment**

<img src="https://img.shields.io/badge/androidstudio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white"> <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">


### Config

<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">


### Development

<img src="https://img.shields.io/badge/android-34A853?style=for-the-badge&logo=android&logoColor=white"> <img src="https://img.shields.io/badge/kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"> <img src="https://img.shields.io/badge/firebase-DD2C00?style=for-the-badge&logo=firebase&logoColor=white"> <img src="https://img.shields.io/badge/node.js-339933?style=for-the-badge&logo=Node.js&logoColor=white"> <img src="https://img.shields.io/badge/Hilt-36474F?style=for-the-badge"> <img src="https://img.shields.io/badge/MVVM-2D50A5?style=for-the-badge">


### Communication

<img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">

## 📺 화면 구성

| 로그인 화면 | 그룹 만들기 화면 | 그룹 들어가기 화면 |
| --- | --- | --- |
| <img width="300" height="600" alt="Screenshot_20250918_031735" src="https://github.com/user-attachments/assets/b86864ce-6f18-453a-93b4-a43f4b5b0449" /> | <img width="300" height="600" src="https://github.com/user-attachments/assets/a1720524-43c0-4971-8e53-6839c0446904" /> | <img width="300" height="600" src= "https://github.com/user-attachments/assets/f8eec8bd-e9b7-41ad-8c03-56141ab3efc0" /> |


| 오늘의 질문 화면 | 요청하기 화면 | 응답하기 화면 |
| --- | --- | --- |
| <img width="300" height="600" src= "https://github.com/user-attachments/assets/f4b0b5ae-d378-48dc-8215-8b620ad836f1" /> | <img width="300" height="600" src= "https://github.com/user-attachments/assets/b410478f-288c-4197-99cc-12705eb1bdf9" />  | <img width="300" height="600" src= "https://github.com/user-attachments/assets/0e23e272-b2e5-4b91-9518-ab688cd62e98" /> | 

| 추억들 화면 | 질문 답변하기 화면 | 마이페이지 화면 |
| --- | --- | --- |
| ![image1](app/src/main/res/drawable/memories.jpg) | ![image2](app/src/main/res/drawable/image8.png) | ![image3](app/src/main/res/drawable/image9.png) |

## 🔔 주요 기능

❗소셜 로그인

- 카카오계정으로 간편하게 계정을 생성하고 로그인할 수 있음
- 빠르고 편리한 앱 이용 가능

❗그룹 생성/들어가기/나가기

- 사용자는 그룹에 가입 및 생성을 해야지만 앱 이용가능
- 사용자는 하나의 그룹만 가입가능

❗현재 나의 모습 공유(요청 및 응답)

- 사용자는 그룹원들에게 사진과 간단한 코멘트를 요청할 수 있음
- 요청 받은 그룹원은 사진과 함께 응답을 남길 수 있음
- 꾸며진 이미지보다 진짜 모습을 공유하는 데 초점을 맞춰 앨범 업로드를 제한하고, 카메라 촬영만 허용

❗질문 및 답변

- 매일 자정 모든 사용자들에게 하루 한 개의 질문을 제공하여 능동적인 참여 유도
- 질문에 답변한 사용자는 다른 그룹원들이 작성한 답변을 볼 수 있음

❗추억들

- 그동안 그룹에 쌓여온 요청과 질문에 대한 답변들을 모아서 볼 수 있음
