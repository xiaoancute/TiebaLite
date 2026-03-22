package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.api.models.TopicDetailBean
import com.huanchengfly.tieba.post.api.models.protos.Anti
import com.huanchengfly.tieba.post.api.models.protos.Error
import com.huanchengfly.tieba.post.api.models.protos.Page
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.frsPage.ForumInfo
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponseData
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponseData
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean
import com.huanchengfly.tieba.post.repository.EmptyDataException
import com.huanchengfly.tieba.post.revival.PublicBrowsePayloadException
import com.huanchengfly.tieba.post.revival.PublicBrowsePayloadGuard
import com.huanchengfly.tieba.post.utils.GsonUtil
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PublicBrowseFailureModeTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun forumPayloadMissingDataFailsClearly() {
        val error = expectThrows<PublicBrowsePayloadException> {
            PublicBrowsePayloadGuard.requireForumPageData(FrsPageResponse())
        }

        assertEquals("吧页返回缺少 data", error.message)
    }

    @Test
    fun forumPayloadMissingForumFailsClearly() {
        val error = expectThrows<PublicBrowsePayloadException> {
            PublicBrowsePayloadGuard.requireForumHeader(
                FrsPageResponse(
                    data_ = FrsPageResponseData(page = Page(current_page = 1))
                )
            )
        }

        assertEquals("吧页返回缺少 forum", error.message)
    }

    @Test
    fun threadPayloadEmptyPostListUsesClearEmptyState() {
        val error = expectThrows<EmptyDataException> {
            PublicBrowsePayloadGuard.requireThreadPageData(
                PbPageResponse(
                    data_ = PbPageResponseData(
                        page = Page(current_page = 1),
                        forum = SimpleForum(id = 1, name = "原神"),
                        anti = Anti(tbs = "tbs"),
                        thread = ThreadInfo(id = 1, title = "title", author = User(id = 2, name = "tester"))
                    )
                )
            )
        }

        assertEquals("帖子页返回空帖子列表", error.message)
    }

    @Test
    fun threadPayloadMissingAntiFailsClearly() {
        val error = expectThrows<PublicBrowsePayloadException> {
            PublicBrowsePayloadGuard.requireThreadPageData(
                PbPageResponse(
                    data_ = PbPageResponseData(
                        page = Page(current_page = 1),
                        forum = SimpleForum(id = 1, name = "原神"),
                        thread = ThreadInfo(id = 1, title = "title", author = User(id = 2, name = "tester")),
                        post_list = listOf(Post(id = 11, floor = 1, author_id = 2))
                    )
                )
            )
        }

        assertEquals("帖子页返回缺少 anti", error.message)
    }

    @Test
    fun hotTopicErrorResponseFailsClearly() {
        val bean = parseHotTopic(
            """
            {
              "no": 7,
              "error": "payload changed"
            }
            """.trimIndent()
        )

        val error = expectThrows<PublicBrowsePayloadException> {
            PublicBrowsePayloadGuard.requireHotTopicEntries(bean)
        }

        assertEquals("热议榜接口返回错误: payload changed", error.message)
    }

    @Test
    fun hotTopicEmptyListDegradesToEmptyEntries() {
        val bean = parseHotTopic(
            """
            {
              "no": 0,
              "error": "success",
              "data": {
                "list": {
                  "ret": []
                }
              }
            }
            """.trimIndent()
        )

        assertTrue(PublicBrowsePayloadGuard.requireHotTopicEntries(bean).isEmpty())
    }

    @Test
    fun hotTopicRouteFallsBackToNestedTopicInfo() {
        val bean = parseHotTopic(
            """
            {
              "no": 0,
              "error": "success",
              "data": {
                "list": {
                  "ret": [
                    {
                      "topic_info": {
                        "topic_id": "28352154",
                        "topic_name": "G2虐爆全场!GEN惨遭碾压",
                        "topic_desc": "GEN"
                      }
                    }
                  ]
                }
              }
            }
            """.trimIndent()
        )

        val route = PublicBrowsePayloadGuard.requireHotTopicRoute(
            PublicBrowsePayloadGuard.requireHotTopicEntries(bean).first()
        )

        assertEquals("28352154", route.topicId)
        assertEquals("G2虐爆全场!GEN惨遭碾压", route.topicName)
    }

    @Test
    fun hotTopicRouteWithoutIdsFailsClearly() {
        val bean = parseHotTopic(
            """
            {
              "no": 0,
              "error": "success",
              "data": {
                "list": {
                  "ret": [
                    {
                      "topic_info": {
                        "topic_desc": "missing ids"
                      }
                    }
                  ]
                }
              }
            }
            """.trimIndent()
        )

        val error = expectThrows<PublicBrowsePayloadException> {
            PublicBrowsePayloadGuard.requireHotTopicRoute(
                PublicBrowsePayloadGuard.requireHotTopicEntries(bean).first()
            )
        }

        assertEquals("热议榜条目缺少 topic_id", error.message)
    }

    @Test
    fun topicDetailErrorResponseFailsClearly() {
        val bean = json.decodeFromString<TopicDetailBean>(
            """
            {
              "no": 3,
              "error": "topic detail failed"
            }
            """.trimIndent()
        )

        val error = expectThrows<PublicBrowsePayloadException> {
            PublicBrowsePayloadGuard.requireTopicDetailPayload(bean)
        }

        assertEquals("话题详情接口返回错误: topic detail failed", error.message)
    }

    @Test
    fun topicDetailMissingTopicInfoFailsClearly() {
        val bean = json.decodeFromString<TopicDetailBean>(
            """
            {
              "no": 0,
              "error": "success",
              "data": {
                "tbs": "123",
                "relate_forum": [],
                "special_topic": [],
                "relate_thread": {
                  "thread_list": []
                },
                "has_more": false
              }
            }
            """.trimIndent()
        )

        val error = expectThrows<PublicBrowsePayloadException> {
            PublicBrowsePayloadGuard.requireTopicDetailPayload(bean)
        }

        assertEquals("话题详情返回缺少 topic_info", error.message)
    }

    @Test
    fun topicDetailMissingOptionalCollectionsDegradesToEmptySections() {
        val payload = PublicBrowsePayloadGuard.requireTopicDetailPayload(
            json.decodeFromString(
                """
                {
                  "no": 0,
                  "error": "success",
                  "data": {
                    "topic_info": {
                      "topic_id": "28352154",
                      "topic_name": "G2虐爆全场!GEN惨遭碾压",
                      "candle": "",
                      "topic_desc": "GEN",
                      "discuss_num": 13075092,
                      "topic_image": "",
                      "share_title": "",
                      "share_pic": "",
                      "is_video_topic": 0
                    },
                    "tbs": "123",
                    "has_more": false
                  }
                }
                """.trimIndent()
            )
        )

        assertEquals("28352154", payload.topicInfo.topicId)
        assertTrue(payload.relatedForums.isEmpty())
        assertTrue(payload.specialTopics.isEmpty())
        assertTrue(payload.relatedThreads.isEmpty())
    }

    @Test
    fun forumPayloadErrorCodeFailsClearly() {
        val error = expectThrows<PublicBrowsePayloadException> {
            PublicBrowsePayloadGuard.requireForumPageData(
                FrsPageResponse(error = Error(error_code = 4, error_msg = "forum denied"))
            )
        }

        assertEquals("吧页接口返回错误: forum denied", error.message)
    }

    @Test
    fun threadPayloadErrorCodeFailsClearly() {
        val error = expectThrows<PublicBrowsePayloadException> {
            PublicBrowsePayloadGuard.requireThreadPageData(
                PbPageResponse(error = Error(error_code = 5, error_msg = "thread denied"))
            )
        }

        assertEquals("帖子页接口返回错误: thread denied", error.message)
    }

    @Test
    fun forumPayloadWithEmptyThreadListStillKeepsPageContext() {
        val data = PublicBrowsePayloadGuard.requireForumPageData(
            FrsPageResponse(
                data_ = FrsPageResponseData(
                    forum = ForumInfo(id = 1, name = "原神"),
                    page = Page(current_page = 1),
                    thread_list = emptyList()
                )
            )
        )

        assertEquals("原神", data.forum?.name)
        assertTrue(data.thread_list.isEmpty())
    }

    private fun parseHotTopic(payload: String): HotMessageListBean =
        GsonUtil.getGson().fromJson(payload, HotMessageListBean::class.java)

    private inline fun <reified T : Throwable> expectThrows(block: () -> Unit): T {
        try {
            block()
        } catch (error: Throwable) {
            if (error is T) {
                return error
            }
            throw AssertionError("expected ${T::class.java.simpleName}, got ${error::class.java.simpleName}", error)
        }
        fail("expected ${T::class.java.simpleName}")
        throw AssertionError("unreachable")
    }
}
