# Workflow

## 1. Adding bot to server

When the bot is added to server, display a welcome message telling admins (have manage channels perm) to DM bot.

## 2. Kdecole auth

When the user dms the bot with any message, the bot will reply with a message telling the user to send in a first message the kdecole username and token in a second message and instructions on how to do so.

To consider: input custom endpoint url as token identification has dupe ids in JKdecole

## 3. Set channel

Once the bot is registered, the user will be told to @mention the bot in the channel they want it to send notifications in.

## 4. Registered

Once the bot is registered, it will periodically check for new articles, emails using the kdecole mobile api and send corresponding notifications. 
