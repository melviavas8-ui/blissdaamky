Daamky rebrand - только изменённые файлы

Как применить к оригинальному проекту Flower:
1. Распакуй этот архив поверх своей папки с сорсами (или замени соответствующие файлы).
2. Удали старые папки (если ещё есть):
   - src/main/java/dev/flower
   - src/main/java/project/weye
   - src/main/resources/assets/flower
   - src/main/resources/flower.mixins.json
3. Переименуй папку assets (если assets/daamky ещё нет):
   mv src/main/resources/assets/flower src/main/resources/assets/daamky
   (в этом архиве уже лежит только код + конфиги, ассеты бинарные не трогались)

Что внутри:
- весь src/main/java (пакеты dev.daamky + project.melvi)
- fabric.mod.json, daamky.mixins.json
- build.gradle (убрана битая recovery-строка)
- settings.gradle, gradle.properties

После этого: ./gradlew clean build
