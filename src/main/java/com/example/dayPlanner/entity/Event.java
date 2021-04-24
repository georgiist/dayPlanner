package com.example.dayPlanner.entity;

public class Event {

    private enum TYPE{
        TASK,
        MEETING
    }
    private enum MARKER{
        PUBLIC,
        PRIVATE,
        PERSONAL
    }
    private int id;
    private String type;
    private String marker;
    private String date;
    private String time;
    private String description;

    public Event(){
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public String  getType() {
        return type;
    }

    public void setType(String type) {
        if(type.equalsIgnoreCase("TASK")){
            this.type = TYPE.TASK.toString();
        }else if(type.equalsIgnoreCase("MEETING")){
            this.type = TYPE.MEETING.toString();
        }else{
            this.type = TYPE.TASK.toString();
        }
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        if(marker.equalsIgnoreCase("PUBLIC")){
            this.marker = MARKER.PUBLIC.toString();
        }else if(marker.equalsIgnoreCase("PRIVATE")){
            this.marker = MARKER.PRIVATE.toString();
        }else if(marker.equalsIgnoreCase("PERSONAL")){
            this.marker = MARKER.PERSONAL.toString();
        }else{
            this.marker = MARKER.PERSONAL.toString();
        }
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
