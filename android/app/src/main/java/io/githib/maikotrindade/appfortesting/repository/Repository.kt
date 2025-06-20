package io.githib.maikotrindade.appfortesting.repository

import io.githib.maikotrindade.appfortesting.model.Notification
import io.githib.maikotrindade.appfortesting.model.Post
import io.githib.maikotrindade.appfortesting.model.Story
import io.githib.maikotrindade.appfortesting.model.User

object Repository {
    internal val currentUser = User(
        username = "Maiko Trindade",
        profilePictureUrl = "https://media.licdn.com/dms/image/v2/D5603AQE6cavpDPSPDA/profile-displayphoto-shrink_800_800/profile-displayphoto-shrink_800_800/0/1730821182721?e=1755734400&v=beta&t=QQp8mOOIlg2iOudjiEhpIAilkPdCoUzyWawimaSsAv4",
        bio = "Android Engineer | OSS | Kotlin | Compose | Testing | Coffee ☕"
    )
    private val userAlice = User(
        username = "Alice Johnson",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Alice+Johnson&background=random&rounded=true",
        bio = "Loves hiking and photography."
    )
    private val userBob = User(
        username = "Bob Smith",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Bob+Smith&background=random&rounded=true",
        bio = "Tech enthusiast and foodie."
    )
    private val userCharlie = User(
        username = "Charlie Lee",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Charlie+Lee&background=random&rounded=true",
        bio = "Runner. Dreamer. Coder."
    )
    private val userDiana = User(
        username = "Diana Prince",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Diana+Prince&background=random&rounded=true",
        bio = "Wonder Woman fan."
    )
    private val userEvan = User(
        username = "Evan Wright",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Evan+Wright&background=random&rounded=true",
        bio = "Music, travel, and books."
    )
    private val userFiona = User(
        username = "Fiona Gallagher",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Fiona+Gallagher&background=random&rounded=true",
        bio = "Coffee lover and bookworm."
    )
    private val userGeorge = User(
        username = "George Martin",
        profilePictureUrl = "https://ui-avatars.com/api/?name=George+Martin&background=random&rounded=true",
        bio = "Guitarist and music producer."
    )
    private val userHannah = User(
        username = "Hannah Kim",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Hannah+Kim&background=random&rounded=true",
        bio = "Traveler. Blogger. Dreamer."
    )
    private val userIan = User(
        username = "Ian Somerhalder",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Ian+Somerhalder&background=random&rounded=true",
        bio = "Nature and animal lover."
    )
    private val userJulia = User(
        username = "Julia Roberts",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Julia+Roberts&background=random&rounded=true",
        bio = "Movie buff and foodie."
    )
    private val userKevin = User(
        username = "Kevin Durant",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Kevin+Durant&background=random&rounded=true",
        bio = "Basketball and sneakers."
    )
    private val userLily = User(
        username = "Lily Collins",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Lily+Collins&background=random&rounded=true",
        bio = "Writer and artist."
    )
    private val userMason = User(
        username = "Mason Clark",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Mason+Clark&background=random&rounded=true",
        bio = "Cyclist and tech geek."
    )
    private val userNina = User(
        username = "Nina Dobrev",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Nina+Dobrev&background=random&rounded=true",
        bio = "Yoga and wellness."
    )
    private val userOscar = User(
        username = "Oscar Isaac",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Oscar+Isaac&background=random&rounded=true",
        bio = "Film and photography."
    )
    private val userPaula = User(
        username = "Paula Abdul",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Paula+Abdul&background=random&rounded=true",
        bio = "Dance and fitness."
    )
    private val userQuinn = User(
        username = "Quinn Hughes",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Quinn+Hughes&background=random&rounded=true",
        bio = "Hockey and gaming."
    )
    private val userRita = User(
        username = "Rita Ora",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Rita+Ora&background=random&rounded=true",
        bio = "Singer and fashionista."
    )
    private val userSam = User(
        username = "Sam Wilson",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Sam+Wilson&background=random&rounded=true",
        bio = "Pilot and adventurer."
    )
    private val userTina = User(
        username = "Tina Fey",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Tina+Fey&background=random&rounded=true",
        bio = "Comedian and writer."
    )
    private val userUma = User(
        username = "Uma Thurman",
        profilePictureUrl = "https://ui-avatars.com/api/?name=Uma+Thurman&background=random&rounded=true",
        bio = "Actress and traveler."
    )

    val stories = listOf(
        Story(user = User("Your story", null), hasStory = false),
        Story(user = userAlice),
        Story(user = userBob),
        Story(user = userCharlie),
        Story(user = userDiana),
        Story(user = userEvan),
        Story(user = userFiona),
        Story(user = userGeorge),
        Story(user = userHannah),
        Story(user = userIan),
        Story(user = userJulia),
        Story(user = userKevin),
        Story(user = userLily),
        Story(user = userMason),
        Story(user = userNina),
        Story(user = userOscar),
        Story(user = userPaula),
        Story(user = userQuinn),
        Story(user = userRita),
        Story(user = userSam),
        Story(user = userTina),
        Story(user = userUma)
    )

    val mediaUrls = listOf(
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/cXblnKXr2BQOaYnTni/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/BY8ORoRpnJDXeBNwxg/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/IwAZ6dvvvaTtdI8SD5/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/P0ZRTYaCmPsJPNd3r0/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/jp7jSyjNNz2ansuOS8/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/hyyV7pnbE0FqLNBAzs/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/DhstvI3zZ598Nb1rFf/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/QTAVEex4ANH1pcdg16/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/ySlraQoZhmeUskuOX5/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/ZHnKJsXLI6ZClYFwzH/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa20xNTMyN2h2dHo5a3NtcXc0c2EwajluOG5mdTNlaXV2b296M253NyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/pK6k4BNalmx44CQj3v/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3dmltZzZ6ZHNncDh1d2R6b292MWg5ZGVtY3M0cnBjOG1hYTBuYTZxZSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/GS9pfaxQj5hPKFGGp8/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3NG1mcDFnem0weGo4NHQ5ZGh1djYxcHR6d3hrMWZ4eGI0MzUydnh4YSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/U4DswrBiaz0p67ZweH/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3NG1mcDFnem0weGo4NHQ5ZGh1djYxcHR6d3hrMWZ4eGI0MzUydnh4YSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/11nloJQzzDP1MA/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3M2k4NGJ1NHZyMzVzMW5wYWVpb3VucmduOTNmN2pjdm52MG81ODAxdSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/122pLlowwMS5aM/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3eW5lM2Zndm9yYTVpN2JqanVtcTZ6MnBqMXNqdmJ2bDFncW9vZXg4dCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/vgUFOWBwBkziE/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3YWo4bDN1eGZ2M3lwNmswMGNlMDVoeWF4Z2ZpOGZrdmg0MXd6MnU5eiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/tkApIfibjeWt1ufWwj/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3M2Zibjl4MXZ0cmw1NHp2eXhnNGdub2hoN3cybXBsZTcwM2c0MWdtYSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/HJB9Nq9RMZgZlLssZF/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3M2Zibjl4MXZ0cmw1NHp2eXhnNGdub2hoN3cybXBsZTcwM2c0MWdtYSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/sT33tck48pfQ9dYkwd/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3dXN3OWFkaG56c2h3ZnI0N2M2NG1wcG5sZmp2bjIzYTI3MXllNDF3ZCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/hjvinhl1pUrb1gdzlV/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3dXN3OWFkaG56c2h3ZnI0N2M2NG1wcG5zZmp2bjIzYTI3MXllNDF3ZCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/hjvinhl1pUrb1gdzlV/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3dXN3OWFkaG56c2h3ZnI0N2M2NG1wcG5zZmp2bjIzYTI3MXllNDF3ZCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/BbzOAkepyC1yUbzlWd/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3N21lMHJ1NGd2N3l6aWFmNDEwbGZ4dGd4eXRueWh5NTUxODVicTM3ZSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/AAsNYlbJpyz7CWXItf/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3dTV0cDN1eWRhZGJmMTZmOHB3dmdzMGlrODZnaHJraXdkNmFwc2JnNSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/FnXgJ5KG1DqhzD0dF6/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3NWZyMW5vZXJycGEwenlqNWVhZHFtZGFsaDh4NGRkaWp0OWhmbm1naiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/Lmosqcsn149zYmhEUh/giphy.gif",
        "https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3NWZyMW5vZXJycGEwenlqNWVhZHFtZGFsaDh4NGRkaWp0OWhmbm1naiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/LlZ2SxbdkCE1rTiTiW/giphy.gif",
    )

    var posts = mutableListOf<Post>().apply {
        val users = listOf(userAlice, userBob, userCharlie, userDiana, userEvan, userFiona, userGeorge, userHannah, userIan, userJulia, userKevin, userLily, userMason, userNina, userOscar, userPaula, userQuinn, userRita, userSam, userTina, userUma)
        val descriptions = listOf(
            "Awesome day!",
            "Look at this!",
            "Random fun",
            "Chilling out",
            "What a view!",
            "So cool!",
            "Just wow, so happy",
            "Vibes",
            "Good times",
            "Epic moment",
            "Feeling great",
            "Adventure happy time",
            "Smile!, be happy!",
            "Unforgettable",
            "Best ever, super happy",
            "Surprise!",
            "Can you believe?",
            "So happy",
            "Random shot",
            "Quick snap"
        )
        repeat(20) { index ->
            val user = users[index % users.size]
            add(
                Post(
                    user = user,
                    mediaUrl = mediaUrls.random(),
                    description = descriptions[index % descriptions.size]
                )
            )
        }
    }

    val notifications = listOf(
        Notification(user = userAlice, action = "liked your photo", time = "2m"),
        Notification(user = userBob, action = "started following you", time = "5m"),
        Notification(user = userCharlie, action = "commented: Nice!", time = "10m"),
        Notification(user = userDiana, action = "liked your post", time = "15m"),
        Notification(user = userEvan, action = "mentioned you in a comment", time = "20m"),
        Notification(user = userAlice, action = "liked your story", time = "25m"),
        Notification(user = userBob, action = "shared your post", time = "30m"),
        Notification(user = userCharlie, action = "liked your photo", time = "35m"),
        Notification(user = userDiana, action = "started following you", time = "40m"),
        Notification(user = userEvan, action = "liked your comment", time = "45m"),
        Notification(user = userAlice, action = "commented: Awesome!", time = "50m"),
        Notification(user = userBob, action = "liked your story", time = "55m"),
        Notification(user = userCharlie, action = "shared your post", time = "1h"),
        Notification(user = userDiana, action = "mentioned you in a comment", time = "1h 5m"),
        Notification(user = userEvan, action = "liked your post", time = "1h 10m"),
        Notification(user = userAlice, action = "started following you", time = "1h 15m"),
        Notification(user = userBob, action = "liked your comment", time = "1h 20m"),
        Notification(user = userCharlie, action = "commented: Great!", time = "1h 25m"),
        Notification(user = userDiana, action = "liked your photo", time = "1h 30m"),
        Notification(user = userEvan, action = "shared your story", time = "1h 35m"),
        Notification(user = userAlice, action = "liked your post", time = "1h 40m"),
        Notification(user = userBob, action = "mentioned you in a comment", time = "1h 45m"),
        Notification(user = userCharlie, action = "liked your story", time = "1h 50m"),
        Notification(user = userDiana, action = "shared your post", time = "1h 55m"),
        Notification(user = userEvan, action = "commented: Well done!", time = "2h"),
        Notification(user = userAlice, action = "liked your comment", time = "2h 5m"),
        Notification(user = userBob, action = "liked your photo", time = "2h 10m"),
        Notification(user = userCharlie, action = "started following you", time = "2h 15m"),
        Notification(user = userDiana, action = "commented: Congrats!", time = "2h 20m"),
        Notification(user = userEvan, action = "liked your post", time = "2h 25m")
    )

    fun getAllUsers(): List<User> = listOf(currentUser, userAlice, userBob, userCharlie, userDiana, userEvan, userFiona, userGeorge, userHannah, userIan, userJulia, userKevin, userLily, userMason, userNina, userOscar, userPaula, userQuinn, userRita, userSam, userTina, userUma)
}