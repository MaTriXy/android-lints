package com.kozaxinan.android.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.kozaxinan.android.checks.ImmutableDataClassDetector.Companion.ISSUE_IMMUTABLE_DATA_CLASS_RULE
import org.junit.Test

@Suppress("UnstableApiUsage")
internal class ImmutableDataClassDetectorTest : LintDetectorTest() {

    @Test
    fun `test data class with val`() {
        lint()
            .files(
                kotlin(
                    """
                  package foo
                  
                  data class Dto(
                      val totalResults: Int,
                      val totalNewResults: Int,
                      private val name: java.lang.String,
                      val bool: Boolean
                  ) {
                  
                    val modifiedName: String
                      get() = name
                  
                    companion object {
  
                      @JvmField
                      val EMPTY = Dto(0, 0, "", false)
                    }
                  }
                """
                ).indented()
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expectClean()
    }

    @Test
    fun `test data class with throwables`() {
        lint()
            .files(
                kotlin(
                    """
                  package foo

                  import java.lang.Exception
                  import java.lang.Throwable
                  
                  data class DtoException(
                      val totalResults: Int,
                      val bool: Boolean,
                      val th: Throwable,
                  ): Exception(message = name)
                """
                ).indented(),
                java(throwable),
                java(exception),
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expectClean()
    }

    @Test
    fun `test data class with String`() {
        lint()
            .files(
                kotlin(
                    """
                  package foo

                  import java.lang.String
                  
                  data class Dto(
                      val someString: String,
                  )
                """
                ).indented(),
                java(string),
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expectClean()
    }

    @Test
    fun `test data class with var`() {
        lint()
            .files(
                kotlin(
                    """
                      package foo
                      
                      data class Dto(
                          val totalResults: Int,
                          var totalNewResults: Int,
                          var name: String,
                          val bool: Boolean
                      )
                """
                ).indented()
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expect(
                """
              src/foo/Dto.kt:3: Warning: totalNewResults is a var. It should be a val in a data class.
              name is a var. It should be a val in a data class. [ImmutableDataClassRule]
              data class Dto(
                         ~~~
              0 errors, 1 warnings
            """
            )
    }

    @Test
    fun `test data class with mutable list`() {
        lint()
            .files(
                kotlin(
                    """
                  package foo
                  
                  data class Dto(
                      val totalResults: Int,
                      val list: MutableList<String>
                  )
                """
                ).indented()
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expect(
                """
              src/foo/Dto.kt:3: Warning: list has a mutable type. Use an immutable type instead. [ImmutableDataClassRule]
              data class Dto(
                         ~~~
              0 errors, 1 warnings
            """
            )
    }

    @Test
    fun `test data class with immutable list`() {
        lint()
            .files(
                kotlin(
                    """
                  package foo
                  
                  data class Dto(
                      val totalResults: Int,
                      val list: List<String>
                  )
                """
                )
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expectClean()
    }

    @Test
    fun `test data class with mutable map`() {
        lint()
            .files(
                kotlin(
                    """
                  package foo
                  
                  data class Dto(
                      val totalResults: Int,
                      val map: MutableMap<String, String>
                  )
                """
                ).indented()
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expect(
                """
              src/foo/Dto.kt:3: Warning: map has a mutable type. Use an immutable type instead. [ImmutableDataClassRule]
              data class Dto(
                         ~~~
              0 errors, 1 warnings
            """
            )
    }

    @Test
    fun `test data class with immutable map`() {
        lint()
            .files(
                kotlin(
                    """
                  package foo
                  
                  data class Dto(
                      val totalResults: Int,
                      val map: Map<String, String>
                  )
                """
                )
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expectClean()
    }

    @Test
    fun `test kotlin class with var`() {
        lint()
            .files(
                kotlin(
                    """
                  package foo
                  
                  class Dto(
                      var totalResults: Int
                  )
                """
                )
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expectClean()
    }

    @Test
    fun `test java class with non final`() {
        lint()
            .files(
                java(
                    """
                  package foo;
                  
                  class Dto {
                    public int totalResults;
                  }
                """
                )
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expectClean()
    }

    @Test
    fun `test kotlin class with var and equals hashcode`() {
        lint()
            .files(
                kotlin(
                    """
                  package foo
                
                  class Dto(var totalResults: Int) {
                    
                    fun hashCode(): Int {
                      return 1
                    }
                    
                    fun equals(obj: Object): Boolean {
                      return false
                    }
                  }
                """
                ).indented()
            )
            .issues(ISSUE_IMMUTABLE_DATA_CLASS_RULE)
            .run()
            .expect(
                """
              src/foo/Dto.kt:3: Warning: totalResults is a var. It should be a val in a data class. [ImmutableDataClassRule]
              class Dto(var totalResults: Int) {
                    ~~~
              0 errors, 1 warnings
            """
            )
    }

    override fun getIssues(): List<Issue> {
        val issues: MutableList<Issue> = ArrayList<Issue>()
        val detectorClass: java.lang.Class<out Detector?> = detectorInstance.javaClass
        // Get the list of issues from the registry and filter out others, to make sure
        // issues are properly registered
        val candidates = IssueRegistry().issues
        for (issue: Issue in candidates) {
            if (issue.implementation.detectorClass === detectorClass) {
                issues.add(issue)
            }
        }
        return issues
    }

    override fun getDetector(): Detector {
        return ImmutableDataClassDetector()
    }

}
