from datetime import datetime
import requests

def get_current_time():
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")

def get_city_weather(city):
    url=f"https://wttr.in/{city}?format=3"
    try:
        response=requests.get(url,timeout=10)
        return response.text.strip()
    except Exception as e:
        return f"{city}: Weather service unavailable ({e})"

#TOOLS=[{},{},...]

TOOLS=[
    {
        "type": "function",
        "function": {
            "name": "get_current_time",
            "description": "Get the current date and time",
            "parameters": {
                "type": "object",
                "properties": {}
            }
        }
    },
    {
        "type":"function",
        "function":{
            "name":"get_city_weather",
            "description":"Returns the current weather for a given city",
            "parameters":{
                "type":"object",
                "properties":{
                    "city":{
                        "type":"string",
                        "description":"Name of the city to get the weather for"     
                   }
                }
            }
                    
        }
        
    }
]

FUNCTION_TO_MAP = {
    "get_current_time": get_current_time,
    "get_city_weather": get_city_weather
}