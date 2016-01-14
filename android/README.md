# Butler: Android

Butler is an Android voice assistant. It's still in alpha, so please don't be rough.

## Intro

Butler promises to be an intelligent assistant, which doesn't only give answers whenever his user wants, but also ask.
It interactively questions and answers. It learns from the user. It learns their behaviour, then starts making assumptions,
predictions, recommendations. It remembers things.
 
What I've done so far, are the question answering part. I hope to finish soon the prediction part.

Given the way it was programmed, Butler is not just an assistant with in-code answers. You can easily add questions
and answers to it in the XML files (I've been thinking about moving the processing part of the app in the cloud,
then it will be easier to define questions/answers remotely). It's also kind of like a framework. I haven't defined many 
questions/answers/commands in the XML files (I'm still thinking about this, probably I'll outsource them) because I focused
on features rather than data, but you can surely add many of them and make Butler a robust know-it-all assistant.

## Comparison with other assistants

Even though I haven't played that much with other assistants, I'll try to make a comparison between them. I'm going to
exclude the unique features that other assistants have, but Butler *will* have soon.

### Let's compare Butler to S-Voice:

- You can ask S-Voice many questions, and it will answer most of them; but even though, it doesn't have an answer for everything
    - Chit chat questions (e.g. "Hello", "How are you?"): if provided with the same data, Butler can answer them too just as well
    - General knowledge questions: I'm not sure about S-Voice, but Butler can be configured to answer questions by
    searching on the Freebase API, the same API used by Google for their search results' info cards
- Commands
    - [According to Wikipedia](http://en.wikipedia.org/wiki/S_Voice): S-Voice can call, text, save notes,
    make appointments, set alarms, update social network websites and offer navigation. So far, Butler only offers
    calling and texting. I will add other commands after I finish implementing new features (I try to focus on adding
    things that make Butler unique, rather than copy other assistants).
    - S-Voice has hot-word detection. Although it works only while the screen is off and few device models support it, 
    it's pretty cool. With a bit of help from a OEM company, Butler can be made to wake up on hot-word detection too.
    BUT, there's another solution. Its only downside is that it's very hacky and it requires root. If you install the
    Xposed Framework and the [Hi Google](http://forum.xda-developers.com/showthread.php?t=2687575) application, you can
    make Butler respond to "Hi Galaxy" commands. 
- UI/UX
    - S-Voice looked a bit ugly until the S5 update, but right now it looks fine I believe. I haven't used it, so I can't
    comment on the UX. I'd say it's OK for TouchWiz (which, by the way, I dislike) users. Butler on the other side, has quite a nice UI. I tried using the new
    [Material Design](http://www.google.com/design/spec/material-design/introduction.html) from Google. On my to-do list
    there's a feature which would allow Butler to change his colours either based on the user's sentiment or however the
    user prefers. As for the UX, I think it's not done yet. I tried using animations to make it feel responsive, but
    it still needs a bit of professional polishing. I'm not a UX guy.
    
### Now, the real competitor... Siri

- Siri gives funny answers and answers most of the questions
    - Chit chat questions: I still believe that given the same data, Butler can be just as good
    - General knowledge questions: Siri uses Wolfram Alpha (and probably others of which I'm not aware); it gives good
    answers, and I believe it's superior to Butler. If I were given all the questions that Siri can answer to, probably
    I'd have to do a bit more work than just write them into the XML files
- Commands – This is the same as with S-Voice. Siri can do lots of things, but Butler can't. Nope, not yet.
- UI/UX – Incomparable (yet). Siri looks like a state-of-the-art piece of software. Butler will need some work to get there.
     
### The voice assistant I use from time to time: Google Now

- Google Now doesn't like chatting; I believe that this is essential for users to build up a relationship between them and 
the assistant. Like what Siri does
- Commands – Google Now focuses on search, but it can also do some commands like calling, texting, note taking etc.
- UI/UX – The same, Butler needs polishing

### I can't compare it to Cortana because I don't like Windows Phone and I've never used it.

## Overall

Things which Butler can do right now, other than those that other assistants can do: None.

Unique things which Butler will be able to do:

- Learn and predict the user's behaviour (probably Google Now does this a bit, but I want to take it even further) 
- Give a unique take on personal assistants (Imagine: you tell Butler you want to see a movie, he suggests one nearby and
asks which friends you'd like to go with. Then Butler tells your friends' assistants about the movie. Then each friend's
assistant will inform the friend about it. All by speaking, no SMS texting or anything else. Just assistants communicating with each other.
But let's take this even further. Imagine that you want to know something, ask your assistant, but he doesn't know. He then asks
other assistants for an answer to your question, and gets back to you with an answer. This is an incredible knowledge-sharing model
since it's more dynamic, richer in content and personalised)
- Interactivity. It's not only the user who asks, period. We're humans, and assistants must be as capable as us.
Making the assistant question, suggest, talk back, is a step forwards towards a perfect assistant – one you wouldn't be
able to distinguish from a real human being).

# Features

- Sentiment based answers: the assistant responds differently depending on the positivity or negativity of the user's 
input
- Answer data is stored in XML files: The format of the data allows for infinite definition of conversations. E.g. 
one can define a user question for which the assistant will give a particular answer for which it will expect a 
particular input to which it will respond in a particular way, most probably different from the way it would have 
responded hadn't the previously defined questions/answer been given
- Patterns can be defined either in REGEX or SEMGREX format. TREGEX coming soon!
- Since it's connected to the Freebase API, it can answer questions about more than 44 million entities (things, persons etc.).
Note: certain commands need to be defined first to make use of all that information.
 
## WIP Features

I hope to finish them until the camp.

- Assistant inter-communication (assistant's can communicate with each other)
- Behaviour prediction (the assistant tries to predict the user's behaviour based on past actions)
- Dynamic answer probability (avoids giving the same answer twice; the assistant "prefers" some answers over others)

## Future features

- Personalities (make each assistant unique, shaped by the user's actions)
- Themes (app colour changes)

# Building the app

Butler needs his companion backend, CoreNlpServer, which provides some NLP features like semantic parsing,
gender determination, sentiment analysis. It's big in size because of the CoreNLP models data.

I'm currently working on another backend based on the actor model, which will be used for assistant inter-communication.
CoreNlpServer will be merged into it. The source of this server hasn't been attached. 

## Debug build

By default: the debug build is set to connect to an AWS instance on which CoreNlpServer runs. Strict mode is activated, proguard is not.

Use gradle to build the apk. To run it locally, make sure to edit the `App/build.gradle` file and replace
`CORENLP_SERVER_AWS` with `CORENLP_SERVER_LOCAL` in the debug build configuration DSL. Then write the correct LAN IP of
the CoreNlpServer in `App/gradle.properties`. Some commands may not work offline (e.g. the ones which need Freebase).

Voice recognition *should* work offline. If it doesn't, consult this [post](http://stackoverflow.com/a/17674655/1133344).
Please note that while offline, the speech recognition accuracy is significantly lower.

## Release

By default: the debug build is set to connect to an AWS instance on which CoreNlpServer runs. Strict mode is disable, proguard is enabled.

In order to sign the app, you'll also need to have an appropriate `App/keystore.properties` file, configured according
to this [post](http://stackoverflow.com/a/20573171/1133344).

# Using the app

## Requirements 

- A device with Android 4.1 or newer is required. Android L is **IDEAL**
- Make sure you have a Text-To-Speech engine installed. The app will use the default one
- To prevent weirdness, please set the male voice as the default. Butler is a male ;)

## Questions
- To see the list of all the available questions Butler can answer to, please look into the XML files in `App/src/main/res/raw`
- After you edit an XML file, make sure you wipe the app's data or do a clean reinstall, otherwise the new questions won't be acknowledged by Butler

# The code

So far this is the biggest Java app I've ever programmed. Most of the codebase is commented, but it still needs some 
explanations of how everything works together. Please consult the javadoc if you feel like so, or just dive into
the source. 

## Logic

`MainActivity` is the only activity of the app. It's like a hub for all the other components.

When the user says something, speech is recognised in real-time using the SpeechRecognizer provided by Android. After the
user finishes speaking, the input is sent to the `MainProcessor` class. It then runs several other processors on the input,
until one of them returns a valid result. Then the result is passed back to `MainActivity`, where it's added to a queue.
The queue is responsible for sending items to the list adapter and speaking them one by one.

## Libraries

I don't own:

- The files in `App/libs/`
- The libraries declared as dependencies in `App/build.gradle`
- Any piece of code commented with an `@source`

Everything else is my own work.

# IMPORTANT: Privacy note

The app has Mixpanel, New Relic and Crashlytics integration.

- Mixpanel tracks users. Questions, answers and personally identifiable information is sent to Mixpanel. E.g. if you
tell Butler your name, it'll be sent to Mixpanel (which means that *I* can see it, it's not Mixpanel the threat here)  
- New Relic tracks application performance, stats are completely anonymous (afaik)
- Crashlytics tracks crashes, reports are semi-anonymous (your user ID is included, which means I can look into Mixpanel and find you)
- Also, CoreNlpServer stores *only* what it processes into a database. If you use the one hosted on AWS, I'll be able to see that too  

Long story short, I'll see what you ask Butler. If you tell him your name, I'll know what you asked him.
I just thought it'd be common sense to make this explicit. If you wish to disable this, just remove the Mixpanel API key from
`App/src/main/java/com/aluxian/butler/utils/Constants.java` AND make sure you use a local instance of CoreNlpServer.

# Testers wanted

I've created a Google+ community and started distributing the app as an alpha release in the Play Store.
Please join the [community](https://plus.google.com/communities/115930872529879470615). It'll help me improve the app from
this early stage.
