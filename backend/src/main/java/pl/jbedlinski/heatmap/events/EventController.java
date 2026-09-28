package pl.jbedlinski.heatmap.events;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<HeatmapPoint> getPoints(@RequestParam String name,
                                        @RequestParam String tag,
                                        @RequestParam(defaultValue = "5") int count) {
        return eventService.getPoints(name, tag, count);
    }
}
