package com.github.woooking.cosyn

import com.github.woooking.cosyn.code.Context

package object qa {

    case class StartSession(context: Context, description: String)

    case class Answer(sessionId: Long, input: String)

}
