package com.focusflow.app
import org.junit.Assert.assertEquals
import org.junit.Test
class DurationTest{@Test fun formatsMinutesAndSeconds(){assertEquals("25:00",formatDuration(1500));assertEquals("00:09",formatDuration(9))}}
