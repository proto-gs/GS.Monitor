<table>
  <thead>
    <tr>
      <th align="left">OS</th>
      <th align="left">Download</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>Windows</b></td>
      <td>
        <a href="https://github.com/g60373250-wq/GS.Monitor/releases/download/v1.0.1/gs.monitor-1.0.1.exe"><img src="https://img.shields.io/badge/Setup-x64-4682b4" alt="Windows Setup"></a>
      </td>
    </tr>
    <tr>
      <td><b>Linux</b></td>
      <td>
        <a href="https://github.com/g60373250-wq/GS.Monitor/releases/download/v1.0.1/gs.monitor_1.0.1_amd64.deb"><img src="https://img.shields.io/badge/DebPackage-x64-d35400?logo=debian" alt="Linux Deb ARM"></a>
      </td>
    </tr>
  </tbody>
</table>

<br>

<details>
  <summary>📸 Посмотреть скриншоты (нажмите, чтобы развернуть)</summary>
  
  <p align="center">
    <img src="https://github.com" alt="Скриншот 1" width="30%">
    <img src="https://github.com" alt="Скриншот 2" width="30%">
    <img src="https://github.com" alt="Скриншот 3" width="30%">
    <br><br>
    <img src="https://github.com" alt="Скриншот 4" width="30%">
    <img src="https://github.com" alt="Скриншот 5" width="30%">
  </p>
</details>







# GS.Monitor

Десктопное приложение для мониторинга, созданное на базе Jetpack Compose Multiplatform (Desktop).

## Требования
* JDK 17 или выше

## Запуск проекта в режиме разработки
Выполните команду в терминале:
./gradlew run

## Сборка готового приложения (дистрибутива)
Для создания установочного пакета (.exe или .deb) под текущую ОС выполните:
./gradlew packageDistributionForCurrentOS
