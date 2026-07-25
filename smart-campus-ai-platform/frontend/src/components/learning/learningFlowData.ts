export interface QuizQuestion {
  question: string;
  options: string[];
  answer: string;
}

export interface CourseTopic {
  title: string;
  description: string;
  content: string[];
  questions: QuizQuestion[];
}

export interface SubjectCourse {
  title: string;
  description: string;
  topics: CourseTopic[];
}

export interface SubjectProgress {
  unlockedTopicIndex: number;
  currentTopicIndex: number;
  completedTopicIndexes: number[];
  bestScores: Record<number, number>;
}

export type ProgressStore = Record<string, SubjectProgress>;

export const FOCUS_DURATIONS = [10, 15, 20];
export const PROGRESS_STORAGE_KEY = "learning-flow-duolingo-progress-v1";

export const COURSE_CONTENT: SubjectCourse[] = [
  {
    title: "Machine Learning",
    description: "Build intuition from foundations to neural networks through short guided topic sprints.",
    topics: [
      {
        title: "Basics",
        description: "Understand what machine learning is, where it fits, and its main workflow.",
        content: [
          "Machine learning lets systems learn patterns from data instead of relying only on hardcoded rules.",
          "A simple workflow is: collect data, clean it, train the model, evaluate results, and improve with iteration."
        ],
        questions: [
          { question: "Machine learning mainly helps systems:", options: ["Learn from data", "Print faster", "Delete files", "Avoid storage"], answer: "Learn from data" },
          { question: "An ML workflow usually starts with:", options: ["Collecting data", "Deploying the app", "Deleting labels", "Drawing charts"], answer: "Collecting data" },
          { question: "Features in ML are:", options: ["Input variables", "Output labels", "Errors only", "Servers"], answer: "Input variables" }
        ]
      },
      {
        title: "Supervised Learning",
        description: "Work with labeled examples for classification and regression tasks.",
        content: [
          "Supervised learning uses labeled datasets where each example already has the correct output.",
          "Classification predicts categories, while regression predicts continuous values such as marks or prices."
        ],
        questions: [
          { question: "Supervised learning needs:", options: ["Labeled data", "Only images", "Encrypted files", "No outputs"], answer: "Labeled data" },
          { question: "Predicting exam marks is:", options: ["Regression", "Classification", "Clustering", "Hashing"], answer: "Regression" },
          { question: "Spam or not spam is:", options: ["Classification", "Regression", "Scheduling", "Compression"], answer: "Classification" }
        ]
      },
      {
        title: "Neural Networks",
        description: "See how layers and backpropagation help model complex patterns.",
        content: [
          "Neural networks use neurons arranged in input, hidden, and output layers.",
          "Backpropagation updates weights after comparing predictions with correct answers."
        ],
        questions: [
          { question: "Hidden layers mainly:", options: ["Capture patterns", "Store files", "Encrypt data", "Sort records"], answer: "Capture patterns" },
          { question: "Weight updates happen through:", options: ["Backpropagation", "Compaction", "Pagination", "Round robin"], answer: "Backpropagation" },
          { question: "The final prediction comes from the:", options: ["Output layer", "Bias layer", "Cache layer", "Kernel"], answer: "Output layer" }
        ]
      }
    ]
  },
  {
    title: "Operating Systems",
    description: "Master execution flow, scheduling, deadlocks, and memory handling one topic at a time.",
    topics: [
      {
        title: "Process Management",
        description: "Learn process states, PCB, and how the OS manages execution.",
        content: [
          "A process is a program in execution with its own state, resources, and context.",
          "The Process Control Block stores process ID, current state, registers, scheduling data, and memory info."
        ],
        questions: [
          { question: "A process is:", options: ["A program in execution", "A static file", "A device", "A memory chip"], answer: "A program in execution" },
          { question: "Process metadata is stored in the:", options: ["PCB", "CPU", "CLI", "DMA"], answer: "PCB" },
          { question: "A process waiting for I/O is usually in:", options: ["Waiting state", "Running state", "Ready state", "New state"], answer: "Waiting state" }
        ]
      },
      {
        title: "CPU Scheduling",
        description: "Understand ready queues, time slices, and preemptive scheduling.",
        content: [
          "CPU scheduling decides which ready process runs next to improve utilization and fairness.",
          "Round Robin uses time slices, while preemptive schedulers can interrupt a running process."
        ],
        questions: [
          { question: "Round Robin gives each process a:", options: ["Time slice", "Memory page", "Disk block", "Device"], answer: "Time slice" },
          { question: "Preemptive scheduling can:", options: ["Interrupt a running process", "Disable all jobs", "Remove RAM", "Delete queues"], answer: "Interrupt a running process" },
          { question: "One main scheduling goal is lower:", options: ["Waiting time", "Font size", "Battery usage", "Cable length"], answer: "Waiting time" }
        ]
      },
      {
        title: "Deadlocks",
        description: "Recognize circular wait and the classic four conditions.",
        content: [
          "A deadlock occurs when processes wait forever because each is holding a resource needed by another.",
          "The four necessary conditions are mutual exclusion, hold and wait, no preemption, and circular wait."
        ],
        questions: [
          { question: "Deadlock has how many necessary conditions?", options: ["4", "2", "3", "5"], answer: "4" },
          { question: "Circular wait means:", options: ["Processes wait in a loop", "CPU runs faster", "Memory is empty", "Files are copied"], answer: "Processes wait in a loop" },
          { question: "Banker's algorithm is linked with:", options: ["Deadlock avoidance", "CPU caching", "File recovery", "Virtual memory"], answer: "Deadlock avoidance" }
        ]
      },
      {
        title: "Memory Management",
        description: "Handle allocation, paging, and virtual memory safely.",
        content: [
          "Memory management tracks used and free memory while protecting one process from another.",
          "Paging uses frames and pages, while virtual memory lets systems run processes larger than physical RAM."
        ],
        questions: [
          { question: "Paging divides physical memory into:", options: ["Frames", "Stacks", "Threads", "Caches"], answer: "Frames" },
          { question: "Virtual memory allows:", options: ["Larger logical memory usage", "Zero RAM use", "No disk access", "No processes"], answer: "Larger logical memory usage" },
          { question: "Memory management also provides:", options: ["Process protection", "Printer ink", "Battery charging", "Email sync"], answer: "Process protection" }
        ]
      }
    ]
  },
  {
    title: "Data Analytics",
    description: "Go from messy data to meaningful insights with clean, visual, statistical thinking.",
    topics: [
      {
        title: "Data Cleaning",
        description: "Fix missing values, remove duplicates, and standardize messy datasets.",
        content: [
          "Data cleaning improves trust in analysis by handling missing values, duplicates, and invalid formats.",
          "Reliable dashboards and models depend on consistent, validated, and well-documented data."
        ],
        questions: [
          { question: "Cleaning data improves:", options: ["Reliability", "Screen brightness", "Network cables", "CPU clock"], answer: "Reliability" },
          { question: "Removing repeated rows is:", options: ["Deduplication", "Regression", "Sampling", "Encoding"], answer: "Deduplication" },
          { question: "Missing values can be handled by:", options: ["Imputation", "Ignoring always", "Deleting charts", "Renaming files"], answer: "Imputation" }
        ]
      },
      {
        title: "Visualization",
        description: "Choose the right chart to communicate trends and comparisons clearly.",
        content: [
          "Visualization turns numbers into patterns people can quickly understand.",
          "Line charts show trends, bar charts compare categories, and scatter plots show relationships."
        ],
        questions: [
          { question: "A line chart is ideal for:", options: ["Trends over time", "Password storage", "Sorting arrays", "Disk formatting"], answer: "Trends over time" },
          { question: "Scatter plots help show:", options: ["Relationships", "CPU queues", "File ownership", "Authentication"], answer: "Relationships" },
          { question: "Good visualization focuses on:", options: ["Insight and clarity", "Maximum decoration", "Hidden labels", "Unused colors"], answer: "Insight and clarity" }
        ]
      },
      {
        title: "Statistics",
        description: "Use descriptive and inferential ideas to interpret data confidently.",
        content: [
          "Descriptive statistics summarize data with measures like mean, median, and standard deviation.",
          "Inferential statistics help us draw conclusions about larger populations from samples."
        ],
        questions: [
          { question: "Standard deviation describes:", options: ["Spread", "Color", "Sorting order", "Bandwidth"], answer: "Spread" },
          { question: "Inferential statistics help us:", options: ["Draw population conclusions", "Rename columns", "Compress files", "Draw icons"], answer: "Draw population conclusions" },
          { question: "Correlation always means causation is:", options: ["False", "True", "A chart type", "A sorting method"], answer: "False" }
        ]
      }
    ]
  },
  {
    title: "Design and Analysis of Algorithm",
    description: "Strengthen algorithm thinking with complexity, sorting, and graph techniques.",
    topics: [
      {
        title: "Time Complexity",
        description: "Measure growth with Big-O and compare algorithms fairly.",
        content: [
          "Time complexity explains how running time grows as the input size increases.",
          "Big-O highlights the dominant growth term such as O(1), O(log n), O(n), or O(n^2)."
        ],
        questions: [
          { question: "Big-O describes:", options: ["Growth rate", "Disk size", "Font style", "Voltage"], answer: "Growth rate" },
          { question: "Which grows faster for large n?", options: ["O(n^2)", "O(log n)", "O(1)", "O(n)"], answer: "O(n^2)" },
          { question: "In O(n^2 + n), the dominant term is:", options: ["n^2", "n", "1", "log n"], answer: "n^2" }
        ]
      },
      {
        title: "Sorting Algorithms",
        description: "Compare efficient ways to order data based on speed and stability.",
        content: [
          "Sorting algorithms organize data ascending or descending using different trade-offs.",
          "Merge sort is guaranteed O(n log n), while quicksort depends strongly on pivot selection."
        ],
        questions: [
          { question: "Merge sort guarantees:", options: ["O(n log n)", "O(n^2)", "O(1)", "O(n^3)"], answer: "O(n log n)" },
          { question: "Quicksort performance is strongly affected by:", options: ["Pivot selection", "Monitor size", "Battery", "Disk label"], answer: "Pivot selection" },
          { question: "Bubble sort is known as:", options: ["Simple but inefficient", "Always fastest", "Graph based", "Only recursive"], answer: "Simple but inefficient" }
        ]
      },
      {
        title: "Graph Algorithms",
        description: "Traverse graphs and solve path or spanning tree problems.",
        content: [
          "Graphs model relationships using vertices and edges in networks, maps, or dependencies.",
          "BFS explores level by level, DFS explores deeply, and Prim or Dijkstra solve specialized graph problems."
        ],
        questions: [
          { question: "BFS explores:", options: ["Level by level", "Only the root", "Randomly", "Only weighted edges"], answer: "Level by level" },
          { question: "Prim's algorithm builds a:", options: ["Minimum spanning tree", "Shortest path", "Binary search tree", "Hash table"], answer: "Minimum spanning tree" },
          { question: "Dijkstra's algorithm is used for:", options: ["Shortest path", "Sorting", "Paging", "CPU scheduling"], answer: "Shortest path" }
        ]
      }
    ]
  }
];

export const normaliseSubjectProgress = (subject: SubjectCourse, progress?: Partial<SubjectProgress>): SubjectProgress => {
  const totalTopics = subject.topics.length;
  const completedTopicIndexes = Array.from(new Set((progress?.completedTopicIndexes ?? []).filter((index) => index >= 0 && index < totalTopics))).sort((left, right) => left - right);
  const fallbackUnlocked = completedTopicIndexes.length >= totalTopics ? Math.max(totalTopics - 1, 0) : completedTopicIndexes.length;

  return {
    unlockedTopicIndex: Math.min(Math.max(progress?.unlockedTopicIndex ?? fallbackUnlocked, 0), Math.max(totalTopics - 1, 0)),
    currentTopicIndex: Math.min(Math.max(progress?.currentTopicIndex ?? fallbackUnlocked, 0), Math.max(totalTopics - 1, 0)),
    completedTopicIndexes,
    bestScores: progress?.bestScores ?? {}
  };
};

export const buildInitialProgress = (): ProgressStore => {
  if (typeof window === "undefined") {
    return Object.fromEntries(COURSE_CONTENT.map((subject) => [subject.title, normaliseSubjectProgress(subject)]));
  }

  try {
    const raw = window.localStorage.getItem(PROGRESS_STORAGE_KEY);
    const parsed = raw ? (JSON.parse(raw) as ProgressStore) : {};
    return Object.fromEntries(COURSE_CONTENT.map((subject) => [subject.title, normaliseSubjectProgress(subject, parsed?.[subject.title])]));
  } catch {
    return Object.fromEntries(COURSE_CONTENT.map((subject) => [subject.title, normaliseSubjectProgress(subject)]));
  }
};
