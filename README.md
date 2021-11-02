# TelegramReimporter
TelegramReimporter is a two-step utility created to import an exported telegram chat.
This can be usefull in  situations where a chat has been deleted but there is an exported folder (or multiple ones) of it.

## Steps:
1. Generate a JSON style export of a conversation in telegram.
2. Configure the "ImportCreator" script with your own paths.
3. Execute the script, it will generate a .zip with the necesary resources.
4. Compile and install the app, you can use a real device or a AVD one.
5. Copy the .zip to the device.
6. Run the app and select the .zip with the "Select Zip" button and wait untill the loaing spinner disapears.
7. In the dropdown menu select the first pack and then select "Send to telegram". 
  > Currently the "Auto send" function does not work.
8. In the android sharing menu, select Telegram itself and then the conversation you want to import.
9. Once the pack is imported, return to the app, if the timer hasn't completed, wait until the timer ends.
10. Start step 7 again with the next package.

## FAQ:
### Is the code reliable?
Maybe, this utility was created for an specific case, it worked, but maybe in your case you need to tweaks some parts of the code to make it work.


### Why do you need diferent packs? Can't it be done in one shot?
Sadly no, android itself hace a limit to how much data can be sent in a parcel between apps. So it must be done in diferent packs.


### Why there is a timer between packs?
Telegram have a ~5 minutes cooldown between imports to the same chat.


### I already have other messages in the chat I want to import, what can i do?
The script is prepared so it can handle multiple export of the same chat, you can create a new export, and added in the script. Then after start importing, you should clear the chat.

Disclaimer: I don't kwon what can happen it there is overlaping between exports, or if it would evem work in your case, I am not responsible of data loss.
