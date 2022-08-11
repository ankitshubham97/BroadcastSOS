# BroadcastSOS
_The SOS app you need_ (Project for Chirp Developer Challenge (https://chirpdevchallenge.devpost.com/))

## Inspiration
There is a personal reason for me to work on this project. I have lost a few friends and family members to road accidents. Indian government report shows that number of deaths caused due to road accident alone is more than 150K per year ([https://ncrb.gov.in/sites/default/files/Chapter-1A-Traffic-Accidents_2019.pdf](https://ncrb.gov.in/sites/default/files/Chapter-1A-Traffic-Accidents_2019.pdf)). This is more than 400 deaths a day. Experts and doctors opine that most of these deaths could be averted if quick action is taken. And the good thing is that with today's technology, it is quite possible.
The project is about developing an SOS system. The ‘SOS system’ is a system aimed at delivering SOS via every routes possible using every technology that is possible and present as of today. The system would be intelligent enough to take care of sending SOS whenever the end user is in distress. 
The scope of the system is huge as I’ll need to make it as inclusive as possible, available for a range of technological landscapes, handling significant amount of corner-cases. So, to begin with, I have defined the scope for this project to be limited to Android users. The MVP would be a very light-weight Android app capable of detecting distress and sending out SOS on twitter through a number of ways. Of course, this thrives on Twitter API v2.

## What it does
It is an app which sends SOS whenever it detects a major physical crash.
SOS is sent in the following ways:
1. broadcasted in the form of tweet
2. twitter DMs to a list of selected people (let’s call this list as ‘emergency list’)

Of course, following could be configured by the user:
1. if they want to have the SOS broadcasted as tweet or not,
2. if they want twitter DMs to be sent to the emergency list
3. who should be included in the emergency list

The content of the tweet and/or the DMs are configurable as well:
1. the text message
2. whether to send current location or not.

In case of every crash, the user gets a notification if they want to rollback the tweet and the sent DMs. If it happens to be a false crash (e.g. the phone just dropped on the floor), the user could just  un-tweet by clicking on the notification. The time to initiate rollback is also configurable by the user.

## How I built it

The app is developed using Android SDK.

### OAuth

The app uses [PIN based 3-legged authorization](https://developer.twitter.com/en/docs/authentication/oauth-1-0a/pin-based-oauth). I chose this approach because I did not want to maintain a separate backend server. However, for development beyond the MVP, I feel I cannot escape this limit. OAuth is needed because the app will be calling write-type APIs of Twitter (e.g. posting tweet, sending DMs etc.) on behalf of the user.

### Background service

The app needs to detect the distress signal every moment; even when the app itself is killed. The way to do this is to spawn a background service when the app is launched and it should not get killed even when the parent process gets killed.

### Twitter API

I used 6 Twitter APIs (5 were v2):

1. sendTweet:
    1. v2 (POST /2/tweets)
    2. To send tweets.
    3. It is used to send SOS tweets.
2. deleteTweet:
    1. v2 (DELETE /2/tweets/${tweetId})
    2. To delete tweets.
    3. It is used to un-tweet in case of false crashes.
3. getBroadcastSosTweets:
    1. v2 (GET /2/tweets/search/recent?query=from%3A${userId}%20has%3Ahashtags%20%22BroadcastSOS%22&sort_order=recency)
    2. To get all tweets that were sent for SOS purpose.
    3. It is used to detect which is the SOS tweet that needs to be deleted in case of false crashes.
4. getFollowers:
    1. v2 (GET /2/users/${userId}/followers)
    2. To get the user’s followers.
    3. It is used to suggest the user who all to add in the emergency list.
5. sendDM:
    1. v1.1 (POST /1.1/direct_messages/events/new.json)
    2. To send DM.
    3. It is used to send DMs to the people in the emergency list.
6. getMe:
    1. v2 (GET /2/users/${userId}?user.fields=name,profile_image_url,username)
    2. To get the user’s profile.
    3. It is used for basic UI for the app.

### UI

The app has a basic 2-fragment UI:

1. Dashboard fragment
2. Settings fragment

## Challenges I ran into

Spawning a never-dying background process was a bit tricky; Android doesn’t really provide a simple way.

## Accomplishments that I’m proud of

I was able to build a working prototype in a matter of few days (could check my commit history: started on 9 Jul 2022 and last commit was on 14 Jul 2022). I started testing the prototype with my friends and it worked as expected.

## What I learned

I learned about Twitter API and that they are really powerful when it comes to building utility solution on top of them; just like this project.

## What's next for BroadcastSOS

As mentioned in the beginning, I see this as an SOS system and is not limited to only Twitter and Android. I intend to expand it by supporting more platforms and using development support from other IM and social media platforms.

## How to run the app
- You can download a pre-built apk from https://drive.google.com/file/d/1-VZBS790Hr3HEXRV71LDm7-htXpfay_Q/view?usp=sharing. It is built on commit [1ca3af421698b16aa8846ce19e22559668e7bbf5][1ca3af421698b16aa8846ce19e22559668e7bbf5]
- You can clone the repo and build your own apk as well.

## Details of the test device
The app was tested on a test device. The [demo][demo] was done on this device itself. The apk used was this: https://drive.google.com/file/d/1-VZBS790Hr3HEXRV71LDm7-htXpfay_Q/view?usp=sharing.
Following are the specifications of the test device:
- Device name: Samsung Galaxy M30
- Android version: 10
- One UI Core: 2.0
- RAM: 4 GB
- Storage: 64 GB

## Important links:
- Demo Video: [demo]
- Link to download apk: [apk]

## Gallery

### Connect the app with your Twitter account!
<img src="https://user-images.githubusercontent.com/16755676/184080432-beecdec3-7632-40a0-8cd1-69ae5c36f3c6.jpeg" alt="connect with twitter" width=30% height=30%>
<hr width=50%>

### Its connected!
<img src="https://user-images.githubusercontent.com/16755676/184081547-7608afe6-97b6-427e-83de-f0fbc0ba907a.jpeg" alt="connected with twitter" width=30% height=30%>
<hr width=50%>

### You can tweak around with the settings here!
<img src="https://user-images.githubusercontent.com/16755676/184080430-6c107ad3-646d-4f90-950b-3779139852e4.jpeg" alt="settings page" width=30% height=30%>
<hr width=50%>

### On detecting distress, it sends an SOS tweet!
<img src="https://user-images.githubusercontent.com/16755676/184080435-211ffdc8-f6cd-4262-b659-5958fc6159dd.jpeg" alt="sends tweet on distress" width=30% height=30%>
<hr width=50%>

### On detecting distress, it also send SOS dms to your close contacts!
<img src="https://user-images.githubusercontent.com/16755676/184080421-5a154ad1-d941-43b0-9508-0803027788df.jpeg" alt="sends dm on distress" width=30% height=30%>
<hr width=50%>

### If it detects a false distress, just click on the notification to rollback the tweet!
<img src="https://user-images.githubusercontent.com/16755676/184080429-d56ee247-c3a0-408e-afb6-ab8f8eb99284.jpeg" alt="rollback if a false distress" width=30% height=30%>
<hr width=50%>




[apk]: <https://drive.google.com/file/d/1-VZBS790Hr3HEXRV71LDm7-htXpfay_Q/view?usp=sharing>
[demo]: <https://www.youtube.com/watch?v=N42q9-64Ylg>
[1ca3af421698b16aa8846ce19e22559668e7bbf5]: <https://github.com/ankitshubham97/BroadcastSOS/commit/1ca3af421698b16aa8846ce19e22559668e7bbf5>
