package com.kutzlerstudios.onboardtrackers.controllers.rest

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/onboard")
class OnboardRest {


    @GetMapping("/tracker/{windows}")
    fun updateTracker(@PathVariable("windows") numOfDrivers: Int){

    }
}