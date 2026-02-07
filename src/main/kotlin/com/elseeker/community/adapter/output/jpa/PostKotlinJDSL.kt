package com.elseeker.community.adapter.output.jpa

import com.elseeker.community.domain.model.Post
import com.elseeker.community.domain.vo.PostStatistics
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.elseeker.member.domain.model.Member
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
import java.time.Instant

object PostKotlinJDSL {

    fun of(
        type: PostType?,
        status: PostStatus?,
        keyword: String? = null,
        author: String? = null,
    ): SelectQuery<Post> {
        return jpql {
            select(
                entity(Post::class)
            ).from(
                entity(Post::class),
                fetchJoin(Post::author),
            ).whereAnd(
                type?.let { path(Post::type).eq(it) },
                status?.let { path(Post::status).eq(it) },
                keyword?.let {
                    or(
                        path(Post::title).like(it),
                        path(Post::content).like(it),
                    )
                },
                author?.let {
                    path(Post::author).path(Member::nickname).like(it)
                },
            ).orderBy(
                path(Post::createdAt).desc()
            )
        }
    }

    fun from(
        sevenDaysAgo: Instant?
    ): SelectQuery<Post> {
        return jpql {
            select(
                entity(Post::class)
            ).from(
                entity(Post::class),
                fetchJoin(Post::author),
            ).whereAnd(
                path(Post::status).eq(PostStatus.PUBLISHED),
                path(Post::createdAt).greaterThanOrEqualTo(sevenDaysAgo),
            ).orderBy(
                path(Post::statistics).path(PostStatistics::score).desc()
            )
        }
    }

    fun of(
        type: PostType?,
        sort: String,
    ): SelectQuery<Post> {
        return jpql {
            select(
                entity(Post::class)
            ).from(
                entity(Post::class),
                fetchJoin(Post::author),
            ).whereAnd(
                path(Post::status).eq(PostStatus.PUBLISHED),
                type?.let { path(Post::type).eq(it) },
            ).orderBy(
                when (sort) {
                    "popular" -> path(Post::statistics).path(PostStatistics::score).desc()
                    else -> path(Post::createdAt).desc()
                }
            )
        }
    }

}
