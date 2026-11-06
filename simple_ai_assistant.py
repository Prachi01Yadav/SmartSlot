import json
import sys
from xml.parsers.expat import model

from openai import OpenAI
from dotenv import load_dotenv
import os

from tools import TOOLS , FUNCTION_TO_MAP


load_dotenv() #by default looks for .env file

client = OpenAI(
    base_url=os.getenv("BASE_URL"), #ollama server url
    api_key=os.getenv("API_KEY")
)


def run_ai_with_tools(system_prompt, user_input):
    """Reusable helper that runs the 2-step LLM + Tool-Calling workflow."""
    chat_messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_input}
    ]
    #1st call to llm api
    response = client.chat.completions.create(
        model=os.getenv("MODEL"),
        messages=chat_messages,
        tools=TOOLS
    )
    msg = response.choices[0].message
    if msg.tool_calls:
        chat_messages.append(msg)
        for tool_call in msg.tool_calls:
            func_name = tool_call.function.name
            func_args = json.loads(tool_call.function.arguments or "{}")
            fn = FUNCTION_TO_MAP.get(func_name)
            result = fn(**func_args)
            chat_messages.append({
                "role": "tool",
                "tool_call_id": tool_call.id,
                "name": func_name,
                "content": str(result)
            })
        #2nd call to llm api
        response = client.chat.completions.create(
            model=os.getenv("MODEL"),
            messages=chat_messages,
            tools=TOOLS
        )
        msg = response.choices[0].message
    return (msg.content or "").strip()


if __name__ == "__main__" and len(sys.argv) > 1:
    # CLI mode used by the Java MVC Controller (SmartSlot)
    # Usage: python simple_ai_assistant.py "<task_title>" "<city>"
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    task_title = sys.argv[1]
    city = sys.argv[2] if len(sys.argv) > 2 else "Pune"
    sys_prompt = (
        "You are SmartSlot, a practical daily task scheduler assistant. "
        "Always check current time and city weather using tools first. "
        "Reply in STRICT single-line format: "
        "TYPE|BEST_SLOT|ADVICE "
        "where TYPE is INDOOR or OUTDOOR, BEST_SLOT is a short time window (e.g. 'Now (6:30 PM)' or 'Tomorrow 8 AM'), "
        "and ADVICE is a helpful, practical tip under 18 words based on the live weather and time."
    )
    prompt = f"Task: '{task_title}' in city: '{city}'. Check current time and weather in {city}, then give TYPE|BEST_SLOT|ADVICE."
    try:
        print(run_ai_with_tools(sys_prompt, prompt))
    except Exception as e:
        print(f"INDOOR|Flexible|AI offline ({e}); schedule based on your priority.")
    sys.exit(0)


messages=[]
messages.append({"role":"system", "content":"You are a funny with great humor person and respond within 20 words always."})
while True:
    
    user_input = input("\nPrachi: ")
    if(user_input.lower() in ["exit", "quit", "bye"]):
        break
    
    messages.append({"role":"user", "content":user_input})
    #1st call to llm api
    response= client.chat.completions.create(
        model=os.getenv("MODEL"),
        messages=messages,
        tools=TOOLS
    )
    
    msg=response.choices[0].message
    if msg.tool_calls:
        messages.append(msg)
        
        for tool_call in msg.tool_calls:
            func_name=tool_call.function.name
            func_args=json.loads(tool_call.function.arguments)
            
            fn=FUNCTION_TO_MAP.get(func_name)
            result = fn(**func_args)    
            
            messages.append({"role":"tool", "tool_call_id":tool_call.id,"name":func_name, "content":str(result)})
            
        #2nd call to llm api
        response = client.chat.completions.create(
            model=os.getenv("MODEL"),
            messages=messages,
            tools=TOOLS
        )
        
        msg=response.choices[0].message
        
    messages.append({
        "role":"assistant",
        "content":msg.content
    })
            
            
    print("Assistant: " + msg.content)
            
    
    # messages.append({"role":"assistant", "content":response.choices[0].message.content})
    # print("Assistant: " + response.choices[0].message.content)

#role=system,user,assistant


#ollama-JSON response bhejta h but openAI ka client isko python object 
#mein convert krdeta h - (ChatCompletion)