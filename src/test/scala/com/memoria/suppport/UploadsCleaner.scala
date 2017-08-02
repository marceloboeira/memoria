package com.memoria.suppport

import com.memoria.Cache
import org.scalatest.{BeforeAndAfterEach, Suite}

trait UploadsCleaner extends BeforeAndAfterEach { this: Suite =>
  override def beforeEach {
    Cache.destroyAll
    super.beforeEach
  }
}
