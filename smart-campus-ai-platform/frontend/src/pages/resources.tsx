import { useEffect, useState } from "react";
import { Card } from "@/components/ui/card";

type Resource = {
  title: string;
  code: string;
  link: string;
};

export default function Resources() {
  const [search, setSearch] = useState("");
  const [usage, setUsage] = useState<Record<string, number>>({});

  const resources: Resource[] = [
    {
      title: "How to Choose the Perfect AI for you?",
      code: "QS005",
      link: "https://www.notion.so/How-to-Choose-the-Perfect-AI-for-you-a700427933448386822d018d8eceab1b?pvs=21",
    },
    {
      title: "Coding Basics Video & AI Toolkit",
      code: "QS006",
      link: "https://www.notion.so/Coding-Basics-Video-AI-Toolkit-92704279334483feab1b0125667160ae?pvs=21",
    },
    {
      title: "ChatGPT Secret Codes for better Outputs",
      code: "QS001",
      link: "https://www.notion.so/ChatGPT-Secret-Codes-for-better-Ouputs-56204279334483e98ba9011368296d71?pvs=21",
    },
    {
      title: "Hackathon Champion's Checklist Pack",
      code: "HM003",
      link: "https://www.notion.so/Hackathon-Champion-s-Complete-Checklist-Pack-Winning-Strategies-02304279334482ff915c015477b0e3f3?pvs=21",
    },
  ];

  // 🔥 Load usage from localStorage
  useEffect(() => {
    const stored = localStorage.getItem("resource_usage");
    if (stored) setUsage(JSON.parse(stored));
  }, []);

  // 🔥 Track clicks
  const handleClick = (res: Resource) => {
    const updated = {
      ...usage,
      [res.title]: (usage[res.title] || 0) + 1,
    };

    setUsage(updated);
    localStorage.setItem("resource_usage", JSON.stringify(updated));

    window.open(res.link, "_blank");
  };

  // 🔍 Search filter
  const filtered = resources.filter((r) =>
    r.title.toLowerCase().includes(search.toLowerCase())
  );

  // 📊 Top used resource
  const topResource = Object.entries(usage).sort(
    (a, b) => b[1] - a[1]
  )[0];

  return (
    <div className="space-y-6">

      {/* 🔥 HEADER */}
      <Card className="p-6 bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-xl">
        <h1 className="text-3xl font-bold">📒 Premium Resources</h1>
        <p className="opacity-80 mt-2">
          Smart curated guides for coding, AI, and career growth 🚀
        </p>
      </Card>

      {/* 📊 ANALYTICS */}
      <div className="grid md:grid-cols-3 gap-4">
        <Card className="p-4 text-center">
          <p className="text-sm text-muted-foreground">Total Resources</p>
          <h2 className="text-2xl font-bold">{resources.length}</h2>
        </Card>

        <Card className="p-4 text-center">
          <p className="text-sm text-muted-foreground">Total Clicks</p>
          <h2 className="text-2xl font-bold">
            {Object.values(usage).reduce((a, b) => a + b, 0)}
          </h2>
        </Card>

        <Card className="p-4 text-center">
          <p className="text-sm text-muted-foreground">🔥 Most Viewed</p>
          <h2 className="text-sm font-semibold">
            {topResource ? topResource[0] : "No data yet"}
          </h2>
        </Card>
      </div>

      {/* 🔍 SEARCH BAR */}
      <Card className="p-4">
        <input
          type="text"
          placeholder="Search resources..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full p-2 border rounded"
        />
      </Card>

      {/* 📚 RESOURCE GRID */}
      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filtered.map((res, index) => (
          <Card
            key={index}
            className="p-4 cursor-pointer hover:shadow-xl transition-all border hover:border-purple-500"
            onClick={() => handleClick(res)}
          >
            <h3 className="font-semibold text-lg">{res.title}</h3>
            <p className="text-sm text-muted-foreground mt-1">
              Code: {res.code}
            </p>

            <div className="mt-3 flex justify-between items-center">
              <span className="text-xs bg-purple-100 text-purple-600 px-2 py-1 rounded">
                Open →
              </span>

              <span className="text-xs text-gray-400">
                👁 {usage[res.title] || 0}
              </span>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}