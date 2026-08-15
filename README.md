<p align="start">
  <a href="https://github.com/proto-gs/GS.Monitor"><img src="https://img.shields.io/badge/Platform-Desktop?style=for-the-badge&logo=kotlin&color=blue" alt="Platform"></a>
  <a href="https://github.com/proto-gs/GS.Monitor/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT?style=for-the-badge&color=green" alt="License"></a>                                                          
  <a href="https://github.com/proto-gs/GS.Monitor/releases"><img src="https://img.shields.io/badge/Release-v1.0.2-orange?style=for-the-badge" alt="Release"></a>
</p>

# GS.Monitor

<p align="start">
  <b>Desktop HTTP traffic analysis built on Jetpack Compose Multiplatform (Desktop).</b>
</p>
<table align="start">
  <thead>
    <tr>
      <th align="center">OS</th>
      <th align="center">Download Link</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="start"><b>Windows</b></td>
      <td align="start">
        <a href="https://github.com/g60373250-wq/GS.Monitor/releases/download/v1.0.2/gs.monitor-1.0.2.exe">
          <img src="https://img.shields.io/badge/Setup-x64-4682b4" alt="Windows Setup">
        </a>
      </td>
    </tr>
    <tr>
      <td align="start"><b>Linux</b></td>
      <td align="start">
        <a href="https://github.com/g60373250-wq/GS.Monitor/releases/download/v1.0.2/gs.monitor_1.0.2_amd64.deb">
          <img src="https://img.shields.io/badge/DebPackage-x64-d35400?logo=debian" alt="Linux Deb">
        </a>
      </td>
    </tr>
  </tbody>
</table>
<details>
  <summary>View Screenshots</summary>
  <br>
  <p align="center">
    <img src="https://github.com/user-attachments/assets/070dbef2-cbf0-4d0c-b49f-5eaffe8a18c9" alt="Скриншот 1" width="80%" style="margin-bottom: 15px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
    <img src="https://github.com/user-attachments/assets/5215acef-86b5-4c52-b8ba-49fd7601a6ab" alt="Скриншот 2" width="80%" style="margin-bottom: 15px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
    <img src="https://github.com/user-attachments/assets/31483ede-fb3a-4edd-8f75-7dab358a94af" alt="Скриншот 3" width="80%" style="margin-bottom: 15px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
    <img src="https://github.com/user-attachments/assets/b9603544-3164-4af2-9ace-84f8c26a050c" alt="Скриншот 4" width="80%" style="margin-bottom: 15px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
    <img src="https://github.com/user-attachments/assets/b9a9ebc8-5059-435d-bb0b-9d28894e4554" alt="Скриншот 5" width="80%" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
  </p>
</details>

# Information
Desktop HTTP traffic analysis using Kotlin/Jetpack Compose.<br><br>
With GS.Monitor, you can:<br><br>
• Instantly check the HTTP status of any website (200 OK, 404 Not Found, etc.).<br><br>
• Analyze technical headers of server responses.<br><br>
• View and format JSON data in a convenient, readable format.<br><br>
• Check cookies set by web resources.<br><br>
The application is extremely minimalist: it does not require registration or collect personal data.<br><br>
Using GS.HTTP, you can also:<br><br>
• Control and manage application settings<br><br>
• Use popular HTTP methods: GET, POST, HEAD, PUT, DELETE, PATCH, OPTIONS, TRACE, CONNECT<br><br>
• Save everything locally in the request history<br><br>
• Find websites by entering their names and find out which domains they are located under in the browser
## Clone repository | Building app | Working with the project

You can work with a project and change it in IDLE alone.
* [Intelij IDEA](https://www.jetbrains.com/idea/)(recommended)<br><br>
Once you have downloaded IDLE from the official website, you can clone the repository using the command<br> `$ git clone https://github.com/proto-gs/GS.Monitor`<br><br>
To make changes to the repository, use the `git add` command. This will save all changes and stage them.<br> Commit with `git commit -m "Your description of the changes"` to commit the changes locally.
<br> Push to GitHub with `git push` to upload files to the server.<br>
Using the `git pull` command, you will pull in all updates from the GitHub repository, provided there are no conflicts in your local project.
For this you will also need git pre-installed.<br><br>
Once you have successfully cloned the repository, all you have to do is compile and run it using `./gradlew`<br>
To run the project from IDLE, use the command `./gradlew run`, or configure the run settings specifically for this application directly in IDLE.<br>
To compile the project for Windows, use the command `./gradlew packageExe` or `./gradlew packageMsi`.<br> To compile the project for Linux/Debian, use the command `./gradlew packageDeb`.<br> To compile a portable version for Linux, use the command `./gradlew packageTarGz`.<br>
To stop compilation, use the command `./gradlew --stop`<br> For help and all the options of `./gradlew` use the command `./gradlew --help`<br><br>

When choosing operating systems, it is recommended to choose Linux.
## License

`GS.Monitor` is licensed under the terms of the MIT License.

For more information, see [LICENSE](/LICENSE) file.

License of components and third-party dependencies it relies on might differ, check `LICENSE` file in the corresponding folder.



