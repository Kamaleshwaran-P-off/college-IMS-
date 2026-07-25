import { useState, useEffect } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { jsPDF } from "jspdf";
import certificateTemplate from "@/assets/certificate.png";

/* ================= SUBJECT DATA ================= */

const SUBJECT_QUIZZES = [
  {
    subject: "Operating System",
    faculty: "Saranya",
    questions: [
      {
        index: 1,
        question: "What is a process?",
        options: ["Program in execution", "File", "Compiler", "OS"],
        answer: "Program in execution",
      },
      {
        index: 2,
        question: "Which scheduling is preemptive?",
        options: ["FCFS", "SJF", "Round Robin", "None"],
        answer: "Round Robin",
      },
      {
        index: 3,
        question: "OS stands for?",
        options: ["Operating System", "Open System", "Order System", "None"],
        answer: "Operating System",
      },
      {
        index: 4,
        question: "Kernel is?",
        options: ["Core of OS", "App", "Compiler", "None"],
        answer: "Core of OS",
      },
      {
        index: 5,
        question: "Paging is?",
        options: ["Memory mgmt", "CPU", "File", "None"],
        answer: "Memory mgmt",
      },
      {
        index: 6,
        question: "Interrupt is?",
        options: ["Signal", "File", "Memory", "None"],
        answer: "Signal",
      },
      {
        index: 7,
        question: "Deadlock occurs when?",
        options: ["Mutual exclusion", "CPU idle", "None", "All"],
        answer: "Mutual exclusion",
      },
      {
        index: 8,
        question: "Virtual memory uses?",
        options: ["Disk", "RAM", "CPU", "None"],
        answer: "Disk",
      },
      {
        index: 9,
        question: "Multitasking means?",
        options: ["Multiple tasks", "Single", "None", "All"],
        answer: "Multiple tasks",
      },
      {
        index: 10,
        question: "Process state?",
        options: ["Running", "Ready", "Waiting", "All"],
        answer: "All",
      },
    ],
  },

  {
    subject: "Design and Analysis of Algorithm",
    faculty: "Jeslin",
    questions: [
      {
        index: 1,
        question: "Binary search complexity?",
        options: ["O(n)", "O(log n)", "O(n²)", "O(1)"],
        answer: "O(log n)",
      },
      {
        index: 2,
        question: "Merge sort?",
        options: ["O(n log n)", "O(n²)", "O(n)", "O(1)"],
        answer: "O(n log n)",
      },
      {
        index: 3,
        question: "DFS uses?",
        options: ["Stack", "Queue", "Array", "None"],
        answer: "Stack",
      },
      {
        index: 4,
        question: "BFS uses?",
        options: ["Queue", "Stack", "None", "All"],
        answer: "Queue",
      },
      {
        index: 5,
        question: "Quicksort worst?",
        options: ["O(n²)", "O(n)", "O(log n)", "None"],
        answer: "O(n²)",
      },
      {
        index: 6,
        question: "Dynamic programming?",
        options: ["Optimization", "Sorting", "Search", "None"],
        answer: "Optimization",
      },
      {
        index: 7,
        question: "Recursion uses?",
        options: ["Stack", "Queue", "Graph", "None"],
        answer: "Stack",
      },
      {
        index: 8,
        question: "Greedy?",
        options: ["Optimal", "Random", "None", "All"],
        answer: "Optimal",
      },
      {
        index: 9,
        question: "Divide & conquer?",
        options: ["Break & solve", "Loop", "None", "All"],
        answer: "Break & solve",
      },
      {
        index: 10,
        question: "Algorithm efficiency?",
        options: ["Time & space", "Speed", "Memory", "None"],
        answer: "Time & space",
      },
    ],
  },

  {
    subject: "Environmental Science",
    faculty: "Vanitha",
    questions: [
      {
        index: 1,
        question: "Sustainability?",
        options: ["Balance", "Pollution", "None", "All"],
        answer: "Balance",
      },
      {
        index: 2,
        question: "Global warming cause?",
        options: ["CO2", "Oxygen", "Water", "None"],
        answer: "CO2",
      },
      {
        index: 3,
        question: "Renewable energy?",
        options: ["Solar", "Coal", "Oil", "Gas"],
        answer: "Solar",
      },
      {
        index: 4,
        question: "Pollution types?",
        options: ["Air", "Water", "Noise", "All"],
        answer: "All",
      },
      {
        index: 5,
        question: "Ozone layer?",
        options: ["Protects", "Harms", "None", "Gas"],
        answer: "Protects",
      },
      {
        index: 6,
        question: "Recycling?",
        options: ["Reuse", "Destroy", "Burn", "None"],
        answer: "Reuse",
      },
      {
        index: 7,
        question: "Greenhouse?",
        options: ["Heat trap", "Cool", "None", "Gas"],
        answer: "Heat trap",
      },
      {
        index: 8,
        question: "Water conservation?",
        options: ["Save", "Waste", "None", "All"],
        answer: "Save",
      },
      {
        index: 9,
        question: "Ecosystem?",
        options: ["Living + non", "Plants", "Animals", "None"],
        answer: "Living + non",
      },
      {
        index: 10,
        question: "Deforestation?",
        options: ["Cutting trees", "Planting", "None", "All"],
        answer: "Cutting trees",
      },
    ],
  },
];

/* ================= COMPONENT ================= */

export default function Quiz() {
  const [selected, setSelected] = useState<any>(null);
  const [started, setStarted] = useState(false);
  const [answers, setAnswers] = useState<any>({});
  const [time, setTime] = useState(600);
  const [startTime, setStartTime] = useState<number | null>(null);
  const [result, setResult] = useState<any>(null);

  const studentName =
    localStorage.getItem("name") || "Student Name";

  /* TIMER */
  useEffect(() => {
    if (!started) return;
    const timer = setInterval(() => {
      setTime((t) => {
        if (t <= 1) {
          clearInterval(timer);
          handleSubmit();
          return 0;
        }
        return t - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [started]);

  const format = (s: number) =>
    `${Math.floor(s / 60)}:${String(s % 60).padStart(2, "0")}`;

  /* SUBMIT */
  const handleSubmit = () => {
    let score = 0;
    selected.questions.forEach((q: any) => {
      if (answers[q.index] === q.answer) score++;
    });

    const timeTaken = startTime
      ? Math.floor((Date.now() - startTime) / 1000)
      : 0;

    setResult({
      score,
      total: selected.questions.length,
      timeTaken,
    });
  };

  /* CERTIFICATE PDF */
  const downloadCertificate = () => {
    const doc = new jsPDF("landscape", "px", "a4");

    doc.addImage(certificateTemplate, "PNG", 0, 0, 842, 595);

    doc.setFont("times", "bold");

    doc.setFontSize(28);
    doc.text(studentName, 421, 300, { align: "center" });

    doc.setFontSize(18);
    doc.text(`Subject: ${selected.subject}`, 421, 350, { align: "center" });

    doc.text(
      `Score: ${result.score}/${result.total}`,
      421,
      380,
      { align: "center" }
    );

    doc.text(
      `Time: ${format(result.timeTaken)}`,
      421,
      410,
      { align: "center" }
    );

    doc.save("certificate.pdf");
  };

  return (
    <div className="p-6 space-y-6">

      {/* SUBJECT SELECTION */}
      {!selected && (
        <div className="grid md:grid-cols-3 gap-4">
          {SUBJECT_QUIZZES.map((s, i) => (
            <Card key={i} className="p-4 cursor-pointer"
              onClick={() => setSelected(s)}>
              <h3 className="font-bold">{s.subject}</h3>
              <p className="text-sm">Faculty: {s.faculty}</p>
            </Card>
          ))}
        </div>
      )}

      {/* START */}
      {selected && !started && !result && (
        <div className="text-center">
          <h2 className="text-xl">{selected.subject}</h2>
          <Button onClick={() => {
            setStarted(true);
            setStartTime(Date.now());
          }}>
            Start Quiz 🚀
          </Button>
        </div>
      )}

      {/* QUESTIONS */}
      {started && !result && (
        <div>
          <div className="font-bold">⏱ {format(time)}</div>

          {selected.questions.map((q: any) => (
            <Card key={q.index} className="p-4 mt-3">
              <p>{q.question}</p>
              {q.options.map((o: string) => (
                <label key={o} className="block">
                  <input
                    type="radio"
                    name={q.index}
                    onChange={() =>
                      setAnswers({ ...answers, [q.index]: o })
                    }
                  />
                  {o}
                </label>
              ))}
            </Card>
          ))}

          <Button onClick={handleSubmit} className="mt-4">
            Submit
          </Button>
        </div>
      )}

      {/* RESULT */}
      {result && (
        <Card className="p-6 text-center">
          <h2 className="text-2xl font-bold">
            Score: {result.score}/{result.total}
          </h2>
          <p>Time: {format(result.timeTaken)}</p>

          <Button className="mt-4" onClick={downloadCertificate}>
            Download Certificate 🎓
          </Button>
        </Card>
      )}
    </div>
  );
} 