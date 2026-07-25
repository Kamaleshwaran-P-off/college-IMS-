import { useCallback, useEffect, useMemo, useState } from "react";
import { Send } from "lucide-react";
import ReactMarkdown from "react-markdown";
import rehypeHighlight from "rehype-highlight";
import remarkGfm from "remark-gfm";
import { Button } from "@/components/ui/button";
import { postJson } from "@/lib/api";
import { cn } from "@/lib/utils";

interface LearningChatProps {
  topicId: number;
  topicTitle: string;
}

type Message = {
  id: string;
  role: "user" | "assistant";
  content: string;
  streaming?: boolean;
};

const suggestions = [
  "Explain this topic",
  "Summarize key points",
  "Generate a quick quiz",
  "Give me study tips"
];

const createId = () => `${Date.now()}-${Math.random().toString(16).slice(2)}`;

export default function LearningChat({ topicId, topicTitle }: LearningChatProps) {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: createId(),
      role: "assistant",
      content: `Ask anything about ${topicTitle}. I will answer using your uploaded material.`
    }
  ]);
  const [input, setInput] = useState("");
  const [isTyping, setIsTyping] = useState(false);

  const canSend = useMemo(() => input.trim().length > 0, [input]);

  useEffect(() => {
    setMessages([
      {
        id: createId(),
        role: "assistant",
        content: `Ask anything about ${topicTitle}. I will answer using your uploaded material.`
      }
    ]);
  }, [topicId, topicTitle]);

  const appendMessage = useCallback((message: Message) => {
    setMessages((prev) => [...prev, message]);
  }, []);

  const updateMessage = useCallback((id: string, updater: (msg: Message) => Message) => {
    setMessages((prev) => prev.map((msg) => (msg.id === id ? updater(msg) : msg)));
  }, []);

  const streamResponse = useCallback(
    (text: string) => {
      const id = createId();
      appendMessage({ id, role: "assistant", content: "", streaming: true });
      const words = text.split(/\s+/).filter(Boolean);
      let index = 0;
      const interval = window.setInterval(() => {
        index += 1;
        updateMessage(id, (msg) => ({
          ...msg,
          content: words.slice(0, index).join(" "),
          streaming: index < words.length
        }));
        if (index >= words.length) {
          window.clearInterval(interval);
        }
      }, 24);
    },
    [appendMessage, updateMessage]
  );

  const sendMessage = async (prompt: string) => {
    if (!prompt.trim()) return;
    appendMessage({ id: createId(), role: "user", content: prompt });
    setInput("");
    setIsTyping(true);
    try {
      const response = await postJson<{ reply: string }>(`/api/student/learning/topics/${topicId}/chat`, {
        message: prompt
      });
      streamResponse(response.reply);
    } catch (err) {
      streamResponse("Sorry, I couldn't answer that right now. Please try again.");
    } finally {
      setIsTyping(false);
    }
  };

  return (
    <div className="space-y-3 rounded-3xl border border-border/60 bg-white/80 p-5 shadow-sm dark:bg-white/10">
      <div>
        <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">AI Tutor</p>
        <h3 className="text-lg font-semibold">Topic companion</h3>
      </div>

      <div className="flex flex-wrap gap-2">
        {suggestions.map((chip) => (
          <button
            key={chip}
            type="button"
            onClick={() => sendMessage(chip)}
            className="rounded-full border border-border/60 bg-white/70 px-3 py-1 text-xs text-muted-foreground transition hover:text-foreground dark:bg-white/5"
          >
            {chip}
          </button>
        ))}
      </div>

      <div className="max-h-[320px] overflow-y-auto rounded-2xl border border-border/60 bg-muted/40 p-4">
        {messages.map((msg) => (
          <div key={msg.id} className={cn("mb-3 flex", msg.role === "user" ? "justify-end" : "justify-start")}> 
            <div className={cn("max-w-[80%] rounded-2xl px-3 py-2 text-sm", msg.role === "user" ? "bg-primary text-primary-foreground" : "bg-white text-foreground dark:bg-white/10")}>
              {msg.role === "assistant" ? (
                <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeHighlight]} className="prose prose-sm max-w-none text-foreground dark:prose-invert">
                  {msg.content}
                </ReactMarkdown>
              ) : (
                <p>{msg.content}</p>
              )}
            </div>
          </div>
        ))}
        {isTyping && (
          <div className="text-xs text-muted-foreground">AI is typing...</div>
        )}
      </div>

      <div className="flex items-center gap-2">
        <input
          className="h-10 flex-1 rounded-xl border border-border bg-white/70 px-3 text-sm dark:bg-white/10"
          placeholder="Ask about this topic..."
          value={input}
          onChange={(event) => setInput(event.target.value)}
        />
        <Button size="sm" onClick={() => sendMessage(input)} disabled={!canSend}>
          <Send size={14} />
        </Button>
      </div>
    </div>
  );
}
