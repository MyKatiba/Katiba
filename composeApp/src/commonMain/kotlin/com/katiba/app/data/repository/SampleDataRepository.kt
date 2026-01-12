package com.katiba.app.data.repository

import com.katiba.app.data.model.*

/**
 * Sample data repository providing static content for UI prototyping.
 * Now integrates with ConstitutionRepository for full constitution data.
 */
object SampleDataRepository {
    
    fun getDailyContent(): DailyContent {
        // Try to get real data from ConstitutionRepository
        if (ConstitutionRepository.isLoaded()) {
            val (chapter, article) = ConstitutionRepository.getRandomArticle()
                ?: return getFallbackDailyContent()

            val clause = article.clauses.firstOrNull() ?: return getFallbackDailyContent()

            return DailyContent(
                id = "daily_${article.number}",
                date = "2026-01-12",
                clause = clause,
                chapterTitle = chapter.title,
                articleTitle = article.title,
                articleNumber = article.number,
                aiDescription = generateAiDescription(article, chapter),
                videoUrl = "",
                videoThumbnailUrl = "",
                educatorName = "Constitutional Expert",
                nextSteps = generateNextSteps(article),
                tips = generateTips(chapter)
            )
        }
        return getFallbackDailyContent()
    }

    private fun getFallbackDailyContent(): DailyContent {
        return DailyContent(
            id = "daily_2026_01_12",
            date = "2026-01-12",
            clause = Clause(
                number = "1",
                text = "Every person has the right to life. The life of a person begins at conception.",
                subClauses = emptyList()
            ),
            chapterTitle = "The Bill of Rights",
            articleTitle = "Right to life",
            articleNumber = 26,
            aiDescription = """
                Article 26 of the Kenyan Constitution protects the most fundamental right of all—the right to life. 
                It states that every person has the right to life and that this life begins at conception.
                
                The Constitution also provides specific circumstances regarding the termination of pregnancy, which can only be done if a trained health professional opinion is that there is need for emergency treatment, or the life or health of the mother is in danger, or if permitted by any other law.
            """.trimIndent(),
            videoUrl = "https://example.com/videos/article26_explained.mp4",
            videoThumbnailUrl = "https://example.com/thumbnails/article26.jpg",
            educatorName = "Hon. Martha Karua",
            nextSteps = listOf(
                "Understand the legal protections for the right to life",
                "Learn about the exceptions provided in the Constitution",
                "Discuss the importance of the Bill of Rights in protecting citizens"
            ),
            tips = listOf(
                "The Bill of Rights is the foundation of our democracy",
                "Every person's life is sacred and protected by law",
                "Know your rights and how to defend them"
            )
        )
    }
    
    private fun generateAiDescription(article: Article, chapter: Chapter): String {
        val clauseTexts = article.clauses.take(2).joinToString("\n\n") { clause ->
            val subClauseText = if (clause.subClauses.isNotEmpty()) {
                "\n" + clause.subClauses.joinToString("\n") { "(${it.label}) ${it.text}" }
            } else ""
            "Clause ${clause.number}: ${clause.text}$subClauseText"
        }

        return """
            Article ${article.number} - ${article.title}
            
            This article is part of Chapter ${chapter.number}: ${chapter.title}.
            
            $clauseTexts
            
            Understanding this article helps you know your constitutional rights and responsibilities as a Kenyan citizen.
        """.trimIndent()
    }

    private fun generateNextSteps(article: Article): List<String> {
        return listOf(
            "Read the full text of Article ${article.number}",
            "Understand how ${article.title} applies to your daily life",
            "Learn about related articles in the Constitution",
            "Discuss with friends and family about these provisions"
        )
    }

    private fun generateTips(chapter: Chapter): List<String> {
        return listOf(
            "Chapter ${chapter.number} covers: ${chapter.title}",
            "The Constitution is the supreme law of Kenya",
            "Every citizen has the right to know and understand the Constitution"
        )
    }

    fun getChapters(): List<Chapter> {
        // Use ConstitutionRepository if loaded, otherwise fall back to sample data
        if (ConstitutionRepository.isLoaded()) {
            return ConstitutionRepository.chapters
        }
        return getSampleChapters()
    }

    private fun getSampleChapters(): List<Chapter> {
        return listOf(
            Chapter(
                number = 1,
                title = "Sovereignty of the People and Supremacy of this Constitution",
                articles = listOf(
                    Article(1, "Sovereignty of the people", listOf(
                        Clause("1", "All sovereign power belongs to the people of Kenya and shall be exercised only in accordance with this Constitution.")
                    )),
                    Article(2, "Supremacy of this Constitution", listOf(
                        Clause("1", "This Constitution is the supreme law of the Republic and binds all persons and all State organs at both levels of government."),
                        Clause("2", "No person may claim or exercise State authority except as authorised under this Constitution.")
                    )),
                    Article(3, "Defence of this Constitution", listOf(
                        Clause("1", "Every person has an obligation to respect, uphold and defend this Constitution."),
                        Clause("2", "Any attempt to establish a government otherwise than in compliance with this Constitution is unlawful.")
                    ))
                )
            ),
            Chapter(
                number = 2,
                title = "The Republic",
                articles = listOf(
                    Article(4, "Declaration of the Republic", listOf(
                        Clause("1", "Kenya is a sovereign Republic."),
                        Clause("2", "The Republic of Kenya shall be a multi-party democratic State founded on the national values and principles of governance referred to in Article 10.")
                    )),
                    Article(5, "Territory of Kenya", listOf(
                        Clause("1", "Kenya consists of the territory and territorial waters comprising Kenya on the effective date, and any additional territory and territorial waters as defined by an Act of Parliament.")
                    )),
                    Article(6, "Devolution and access to services", listOf(
                        Clause("1", "The territory of Kenya is divided into the counties specified in the First Schedule.")
                    )),
                    Article(7, "National, official and other languages", listOf(
                        Clause("1", "The national language of the Republic is Kiswahili."),
                        Clause("2", "The official languages of the Republic are Kiswahili and English.")
                    )),
                    Article(8, "State and religion", listOf(
                        Clause("", "There shall be no State religion.")
                    )),
                    Article(9, "National symbols and national days", listOf(
                        Clause("1", "The national symbols of the Republic are the national flag, the national anthem, the coat of arms, and the public seal.")
                    ))
                )
            ),
            Chapter(
                number = 3,
                title = "Citizenship",
                articles = listOf(
                    Article(10, "Citizenship by birth", listOf(
                        Clause("1", "A person is a citizen by birth if on the day of the person's birth, whether or not the person is born in Kenya, either the mother or father of the person is a citizen.")
                    )),
                    Article(11, "Citizenship by registration", listOf(
                        Clause("1", "A person who has been lawfully resident in Kenya for a continuous period of at least seven years, and who satisfies the conditions prescribed by an Act of Parliament, may apply to be registered as a citizen.")
                    )),
                    Article(12, "Dual citizenship", listOf(
                        Clause("1", "A citizen by birth does not lose citizenship by acquiring the citizenship of another country.")
                    ))
                )
            ),
            Chapter(
                number = 4,
                title = "The Bill of Rights",
                articles = listOf(
                    Article(19, "Rights and fundamental freedoms", listOf(
                        Clause("1", "The Bill of Rights is an integral part of Kenya's democratic state and is the framework for social, economic and cultural policies.")
                    )),
                    Article(20, "Application of Bill of Rights", listOf(
                        Clause("1", "The Bill of Rights applies to all law and binds all State organs and all persons.")
                    )),
                    Article(21, "Implementation of rights and fundamental freedoms", listOf(
                        Clause("1", "It is a fundamental duty of the State and every State organ to observe, respect, protect, promote and fulfil the rights and fundamental freedoms in the Bill of Rights.")
                    )),
                    Article(22, "Enforcement of Bill of Rights", listOf(
                        Clause("1", "Every person has the right to institute court proceedings claiming that a right or fundamental freedom in the Bill of Rights has been denied, violated or infringed, or is threatened.")
                    )),
                    Article(26, "Right to life", listOf(
                        Clause("1", "Every person has the right to life.")
                    )),
                    Article(27, "Equality and freedom from discrimination", listOf(
                        Clause("1", "Every person is equal before the law and has the right to equal protection and equal benefit of the law."),
                        Clause("2", "Equality includes the full and equal enjoyment of all rights and fundamental freedoms.")
                    )),
                    Article(28, "Human dignity", listOf(
                        Clause("1", "Every person has inherent dignity and the right to have that dignity respected and protected.")
                    )),
                    Article(29, "Freedom and security of the person", listOf(
                        Clause("", "Every person has the right to freedom and security of the person, which includes the right not to be detained without trial.")
                    )),
                    Article(30, "Freedom from slavery", listOf(
                        Clause("1", "A person shall not be held in slavery or servitude."),
                        Clause("2", "A person shall not be required to perform forced labour.")
                    )),
                    Article(31, "Privacy", listOf(
                        Clause("", "Every person has the right to privacy, which includes the right not to have their person, home or property searched.")
                    )),
                    Article(32, "Freedom of conscience, religion, belief and opinion", listOf(
                        Clause("1", "Every person has the right to freedom of conscience, religion, thought, belief and opinion.")
                    )),
                    Article(33, "Freedom of expression", listOf(
                        Clause("1", "Every person has the right to freedom of expression, which includes freedom to seek, receive or impart information or ideas.")
                    )),
                    Article(34, "Freedom of the media", listOf(
                        Clause("1", "Freedom and independence of electronic, print and all other types of media is guaranteed.")
                    )),
                    Article(35, "Access to information", listOf(
                        Clause("1", "Every citizen has the right of access to information held by the State.")
                    )),
                    Article(36, "Freedom of association", listOf(
                        Clause("1", "Every person has the right to freedom of association, which includes the right to form, join or participate in the activities of an association of any kind.")
                    )),
                    Article(37, "Assembly, demonstration, picketing and petition", listOf(
                        Clause("", "Every person has the right, peaceably and unarmed, to assemble, to demonstrate, to picket, and to present petitions to public authorities.")
                    )),
                    Article(38, "Political rights", listOf(
                        Clause("1", "Every citizen is free to make political choices, which includes the right to form, or participate in forming, a political party.")
                    )),
                    Article(39, "Freedom of movement and residence", listOf(
                        Clause("1", "Every person has the right to freedom of movement.")
                    )),
                    Article(40, "Protection of right to property", listOf(
                        Clause("1", "Subject to Article 65, every person has the right, either individually or in association with others, to acquire and own property of any description and in any part of Kenya.")
                    )),
                    Article(41, "Labour relations", listOf(
                        Clause("1", "Every person has the right to fair labour practices.")
                    )),
                    Article(42, "Environment", listOf(
                        Clause("", "Every person has the right to a clean and healthy environment, which includes the right to have the environment protected for the benefit of present and future generations.")
                    )),
                    Article(43, "Economic and social rights", listOf(
                        Clause("1", "Every person has the right to the highest attainable standard of health, which includes the right to health care services, including reproductive health care."),
                        Clause("2", "A person shall not be denied emergency medical treatment.")
                    )),
                    Article(44, "Language and culture", listOf(
                        Clause("1", "Every person has the right to use the language, and to participate in the cultural life, of the person's choice.")
                    )),
                    Article(45, "Family", listOf(
                        Clause("1", "The family is the natural and fundamental unit of society and the necessary basis of social order, and shall enjoy the recognition and protection of the State.")
                    )),
                    Article(46, "Consumer rights", listOf(
                        Clause("1", "Consumers have the right to goods and services of reasonable quality.")
                    )),
                    Article(47, "Fair administrative action", listOf(
                        Clause("1", "Every person has the right to administrative action that is expeditious, efficient, lawful, reasonable and procedurally fair.")
                    )),
                    Article(48, "Access to justice", listOf(
                        Clause("", "The State shall ensure access to justice for all persons and, if any fee is required, it shall be reasonable and shall not impede access to justice.")
                    )),
                    Article(49, "Rights of arrested persons", listOf(
                        Clause("1", "An arrested person has the right to be informed promptly, in a language that the person understands, of the reason for the arrest, the right to remain silent, and the consequences of not remaining silent.")
                    )),
                    Article(50, "Fair hearing", listOf(
                        Clause("1", "Every person has the right to have any dispute that can be resolved by the application of law decided in a fair and public hearing before a court or, if appropriate, another independent and impartial tribunal or body.")
                    )),
                    Article(51, "Rights of persons detained, held in custody or imprisoned", listOf(
                        Clause("1", "A person who is detained, held in custody or imprisoned under the law, retains all the rights and fundamental freedoms in the Bill of Rights.")
                    ))
                )
            ),
            Chapter(
                number = 5,
                title = "Land and Environment",
                articles = listOf(
                    Article(60, "Principles of land policy", listOf(
                        Clause("1", "Land in Kenya shall be held, used and managed in a manner that is equitable, efficient, productive and sustainable.")
                    )),
                    Article(61, "Classification of land", listOf(
                        Clause("1", "All land in Kenya belongs to the people of Kenya collectively as a nation, as communities and as individuals.")
                    ))
                )
            ),
            Chapter(
                number = 6,
                title = "Leadership and Integrity",
                articles = listOf(
                    Article(73, "Responsibilities of leadership", listOf(
                        Clause("1", "Authority assigned to a State officer is a public trust to be exercised in a manner that is consistent with the purposes and objects of this Constitution.")
                    )),
                    Article(74, "Oath of office of State officers", listOf(
                        Clause("1", "Before assuming a State office, acting in a State office, or performing any functions of a State office, a person shall take and subscribe the oath or affirmation of office.")
                    )),
                    Article(75, "Conduct of State officers", listOf(
                        Clause("1", "A State officer shall behave, whether in public and official life, in private life, or in association with other persons, in a manner that avoids—any conflict between personal interests and public or official duties.")
                    ))
                )
            ),
            Chapter(
                number = 7,
                title = "Representation of the People",
                articles = listOf(
                    Article(81, "General principles for the electoral system", listOf(
                        Clause("", "The electoral system shall comply with the following principles: freedom of citizens to exercise their political rights; universal suffrage based on the aspiration for fair representation and equality of vote.")
                    )),
                    Article(82, "Legislation on elections", listOf(
                        Clause("1", "Parliament shall enact legislation to provide for the delimitation by the Independent Electoral and Boundaries Commission of electoral units for election of members of the National Assembly and county assemblies.")
                    )),
                    Article(83, "Registration as a voter", listOf(
                        Clause("1", "A person qualifies for registration as a voter at elections or referenda if the person is an adult citizen.")
                    ))
                )
            ),
            Chapter(
                number = 8,
                title = "The Legislature",
                articles = listOf(
                    Article(93, "Establishment of Parliament", listOf(
                        Clause("1", "There is established a Parliament of Kenya, which shall consist of the National Assembly and the Senate.")
                    )),
                    Article(94, "Role of Parliament", listOf(
                        Clause("1", "The legislative authority of the Republic is derived from the people and, at the national level, is vested in and exercised by Parliament.")
                    ))
                )
            ),
            Chapter(
                number = 9,
                title = "The Executive",
                articles = listOf(
                    Article(129, "Principles of executive authority", listOf(
                        Clause("1", "Executive authority derives from the people of Kenya and shall be exercised in accordance with this Constitution.")
                    )),
                    Article(130, "The National Executive", listOf(
                        Clause("1", "The national executive of the Republic comprises the President, the Deputy President and the rest of the Cabinet.")
                    )),
                    Article(131, "Authority of the President", listOf(
                        Clause("1", "The President is the Head of State and Government.")
                    ))
                )
            ),
            Chapter(
                number = 10,
                title = "Judiciary",
                articles = listOf(
                    Article(159, "Judicial authority", listOf(
                        Clause("1", "Judicial authority is derived from the people and vests in, and shall be exercised by, the courts and tribunals established by or under this Constitution.")
                    )),
                    Article(160, "Independence of the Judiciary", listOf(
                        Clause("1", "In the exercise of judicial authority, the Judiciary, as constituted by Article 161, shall be subject only to this Constitution and the law and shall not be subject to the control or direction of any person or authority.")
                    ))
                )
            ),
            Chapter(
                number = 11,
                title = "Devolved Government",
                articles = listOf(
                    Article(174, "Objects of devolution", listOf(
                        Clause("", "The objects of the devolution of government are to promote democratic and accountable exercise of power; to foster national unity by recognising diversity.")
                    )),
                    Article(175, "Principles of devolved government", listOf(
                        Clause("", "County governments established under this Constitution shall reflect the following principles: county governments shall be based on democratic principles and the separation of powers.")
                    )),
                    Article(176, "County governments", listOf(
                        Clause("1", "There shall be a county government for each county, consisting of a county assembly and a county executive.")
                    ))
                )
            ),
            Chapter(
                number = 12,
                title = "Public Finance",
                articles = listOf(
                    Article(201, "Principles of public finance", listOf(
                        Clause("", "The following principles shall guide all aspects of public finance in the Republic: there shall be openness and accountability, including public participation in financial matters.")
                    )),
                    Article(202, "Equitable sharing of national revenue", listOf(
                        Clause("1", "Revenue raised nationally shall be shared equitably among the national and county governments.")
                    ))
                )
            ),
            Chapter(
                number = 13,
                title = "The Public Service",
                articles = listOf(
                    Article(232, "Values and principles of public service", listOf(
                        Clause("1", "The values and principles of public service include high standards of professional ethics; efficient, effective and economic use of resources; responsive, prompt, effective, impartial and equitable provision of services.")
                    ))
                )
            ),
            Chapter(
                number = 14,
                title = "National Security",
                articles = listOf(
                    Article(238, "Principles of national security", listOf(
                        Clause("1", "National security is the protection against internal and external threats to Kenya's territorial integrity and sovereignty, its people, their rights, freedoms, property, peace, stability and prosperity, and other national interests.")
                    )),
                    Article(239, "National security organs", listOf(
                        Clause("1", "The national security organs are the Kenya Defence Forces; the National Intelligence Service; and the National Police Service.")
                    ))
                )
            ),
            Chapter(
                number = 15,
                title = "Commissions and Independent Offices",
                articles = listOf(
                    Article(248, "Commissions and independent offices", listOf(
                        Clause("1", "There shall be the following constitutional Commissions: the Kenya National Human Rights and Equality Commission; the National Land Commission; the Independent Electoral and Boundaries Commission.")
                    ))
                )
            ),
            Chapter(
                number = 16,
                title = "Amendment of this Constitution",
                articles = listOf(
                    Article(255, "Amendment of this Constitution", listOf(
                        Clause("1", "A proposed amendment to this Constitution shall be enacted in accordance with Article 256 or 257.")
                    )),
                    Article(256, "Amendment by parliamentary initiative", listOf(
                        Clause("1", "A Bill to amend this Constitution may be introduced in either House of Parliament.")
                    )),
                    Article(257, "Amendment by popular initiative", listOf(
                        Clause("1", "An amendment to this Constitution may be proposed by a popular initiative signed by at least one million registered voters.")
                    ))
                )
            ),
            Chapter(
                number = 17,
                title = "General Provisions",
                articles = listOf(
                    Article(258, "Enforcement of this Constitution", listOf(
                        Clause("1", "Every person has the right to institute court proceedings, claiming that this Constitution has been contravened, or is threatened with contravention.")
                    ))
                )
            ),
            Chapter(
                number = 18,
                title = "Transitional and Consequential Provisions",
                articles = listOf(
                    Article(262, "Application of this Chapter", listOf(
                        Clause("1", "This Chapter applies to all pending matters under the previous Constitution.")
                    ))
                )
            )
        )
    }
    
    fun getLessons(): List<Lesson> {
        return listOf(
            // Preamble & Chapter 1
            Lesson("lesson_1", "We The People", "Understanding sovereignty and who holds power in Kenya", 1, 1, 50, isCompleted = true, isLocked = false),
            Lesson("lesson_2", "The Supreme Law", "Why the Constitution is the highest law", 1, 2, 50, isCompleted = true, isLocked = false),
            Lesson("lesson_3", "Defending Our Constitution", "Every citizen's duty to protect the Constitution", 1, 3, 50, isCurrent = true, isLocked = false),
            
            // Chapter 2
            Lesson("lesson_4", "Kenya: Our Republic", "The declaration and symbols of our nation", 2, 1, 60, isLocked = false),
            Lesson("lesson_5", "Our Languages", "Kiswahili, English, and cultural languages", 2, 2, 60, isLocked = false),
            Lesson("lesson_6", "Separation of State and Religion", "Understanding secular governance", 2, 3, 60),
            
            // Chapter 3
            Lesson("lesson_7", "Citizenship by Birth", "Who qualifies as a Kenyan citizen", 3, 1, 70),
            Lesson("lesson_8", "Registration & Dual Citizenship", "Acquiring Kenyan citizenship", 3, 2, 70),
            
            // Chapter 4 - Bill of Rights
            Lesson("lesson_9", "Your Fundamental Rights", "Overview of the Bill of Rights", 4, 1, 80),
            Lesson("lesson_10", "Right to Life", "The most fundamental human right", 4, 2, 80),
            Lesson("lesson_11", "Equality for All", "Freedom from discrimination", 4, 3, 80),
            Lesson("lesson_12", "Human Dignity", "Inherent worth of every person", 4, 4, 80),
            Lesson("lesson_13", "Freedom & Security", "Protection from arbitrary detention", 4, 5, 80),
            Lesson("lesson_14", "Privacy Rights", "Protection of personal information", 4, 6, 80),
            Lesson("lesson_15", "Freedom of Expression", "Your right to speak and be heard", 4, 7, 80),
            Lesson("lesson_16", "Political Rights", "Voting and political participation", 4, 8, 80),
            Lesson("lesson_17", "Economic & Social Rights", "Health, education, and housing", 4, 9, 80),
            Lesson("lesson_18", "Access to Justice", "Fair hearings and legal representation", 4, 10, 80),
            
            // Chapter 5
            Lesson("lesson_19", "Land Ownership", "Principles of land in Kenya", 5, 1, 70),
            Lesson("lesson_20", "Environmental Rights", "Clean and healthy environment for all", 5, 2, 70),
            
            // Chapter 6
            Lesson("lesson_21", "Leaders with Integrity", "Standards for public officers", 6, 1, 80),
            
            // More chapters...
            Lesson("lesson_22", "How Parliament Works", "The legislative process", 8, 1, 90),
            Lesson("lesson_23", "The Executive Branch", "Presidential powers and cabinet", 9, 1, 90),
            Lesson("lesson_24", "Judicial Independence", "How courts protect your rights", 10, 1, 90),
            Lesson("lesson_25", "County Governments", "Devolution and local governance", 11, 1, 100),
            Lesson("lesson_26", "Public Money", "Principles of public finance", 12, 1, 100),
            Lesson("lesson_27", "Amending the Constitution", "How to change the supreme law", 16, 1, 120)
        )
    }
    
    fun getUserProfile(): UserProfile {
        return UserProfile(
            id = "user_001",
            name = "Amani Wanjiku",
            email = "amani@example.com",
            county = "Nairobi",
            constituency = "Westlands",
            ward = "Parklands",
            joinedDate = "2024-06-15",
            streak = 7,
            longestStreak = 21,
            totalLessonsCompleted = 15,
            badges = listOf(
                Badge("badge_1", "Constitution Starter", "Completed your first lesson", "", "2024-06-15", true),
                Badge("badge_2", "Rights Champion", "Completed the Bill of Rights chapter", "", "2024-08-20", true),
                Badge("badge_3", "Week Warrior", "Maintained a 7-day streak", "", "2024-12-29", true),
                Badge("badge_4", "Master Scholar", "Complete all lessons", "", null, false),
                Badge("badge_5", "Community Leader", "Share with 10 friends", "", null, false)
            )
        )
    }
    
    fun getRecentActivity(): List<ActivityRecord> {
        return listOf(
            ActivityRecord("act_1", ActivityType.DAILY_CLAUSE_READ, "Read Clause of the Day", "2024-12-29", 10),
            ActivityRecord("act_2", ActivityType.LESSON_COMPLETED, "Completed: The Supreme Law", "2024-12-28", 50),
            ActivityRecord("act_3", ActivityType.STREAK_MILESTONE, "7-Day Streak Achieved!", "2024-12-28", 100),
            ActivityRecord("act_4", ActivityType.LESSON_COMPLETED, "Completed: We The People", "2024-12-27", 50),
            ActivityRecord("act_5", ActivityType.DAILY_CLAUSE_READ, "Read Clause of the Day", "2024-12-26", 10)
        )
    }
}
