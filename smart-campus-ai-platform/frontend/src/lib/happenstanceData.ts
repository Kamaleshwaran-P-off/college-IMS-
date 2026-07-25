export type Opportunity = {
  id: number;
  title: string;
  description: string;
  domain: string;
  link: string;
  platform: string;
  type: "Hackathon" | "Internship" | "Event";
  tags: string[];
  date?: string;
  location?: string;
  trending?: boolean;
  saved?: boolean;
};

export const opportunityTags = [
  "AI",
  "Web Dev",
  "Startup",
  "Design",
  "Data Science",
  "Cybersecurity",
  "Product",
  "IoT",
  "Mobile",
  "Open Source"
];

export const mockOpportunities: Opportunity[] = [
  {
    id: 1,
    title: "AI Campus Hack Sprint",
    description: "Build AI-powered solutions for student life with mentors and quick prizes.",
    domain: "AI",
    link: "https://unstop.com/hackathons",
    platform: "Unstop",
    type: "Hackathon",
    tags: ["AI", "Data Science"],
    date: "Apr 22, 2026",
    location: "Online",
    trending: true
  },
  {
    id: 2,
    title: "Smart City Design Jam",
    description: "Design-first sprint on urban mobility, sustainability, and citizen UX.",
    domain: "Design",
    link: "https://www.knowafest.com/explore/events?city=Chennai",
    platform: "Knowafest",
    type: "Event",
    tags: ["Design", "Product"],
    date: "May 06, 2026",
    location: "Chennai"
  },
  {
    id: 3,
    title: "Startup GTM Fellowship",
    description: "Work with founders on growth experiments and go-to-market strategy.",
    domain: "Startup",
    link: "https://unstop.com/internships",
    platform: "Unstop",
    type: "Internship",
    tags: ["Startup", "Product"],
    date: "Rolling",
    location: "Remote"
  },
  {
    id: 4,
    title: "Full Stack Build Weekend",
    description: "Ship a full-stack MVP in 48 hours with live demos and judges.",
    domain: "Web Dev",
    link: "https://unstop.com/hackathons",
    platform: "Unstop",
    type: "Hackathon",
    tags: ["Web Dev", "Open Source"],
    date: "May 19, 2026",
    location: "Online",
    trending: true
  },
  {
    id: 5,
    title: "Data Science Micro-Internship",
    description: "Analyze campus data, build dashboards, and present findings to faculty.",
    domain: "Data Science",
    link: "https://unstop.com/internships",
    platform: "Unstop",
    type: "Internship",
    tags: ["Data Science", "AI"],
    date: "Apr 30, 2026",
    location: "Remote"
  },
  {
    id: 6,
    title: "Cybersecurity Capture the Flag",
    description: "Hands-on CTF for incident response and red-team fundamentals.",
    domain: "Cybersecurity",
    link: "https://www.knowafest.com/explore/events?city=Chennai",
    platform: "Knowafest",
    type: "Event",
    tags: ["Cybersecurity", "AI"],
    date: "May 25, 2026",
    location: "Chennai"
  },
  {
    id: 7,
    title: "IoT for Agriculture Challenge",
    description: "Build sensor-driven prototypes for precision agriculture.",
    domain: "IoT",
    link: "https://unstop.com/hackathons",
    platform: "Unstop",
    type: "Hackathon",
    tags: ["IoT", "Product"],
    date: "Jun 02, 2026",
    location: "Online"
  },
  {
    id: 8,
    title: "Mobile UX Sprint",
    description: "Craft mobile UX flows for campus apps with quick feedback loops.",
    domain: "Mobile",
    link: "https://www.knowafest.com/explore/events?city=Chennai",
    platform: "Knowafest",
    type: "Event",
    tags: ["Design", "Mobile"],
    date: "Jun 12, 2026",
    location: "Chennai"
  },
  {
    id: 9,
    title: "Open Source Contributor Drive",
    description: "Join open source maintainers, fix issues, and earn badges.",
    domain: "Open Source",
    link: "https://unstop.com/competitions",
    platform: "Unstop",
    type: "Event",
    tags: ["Open Source", "Web Dev"],
    date: "May 30, 2026",
    location: "Online"
  },
  {
    id: 10,
    title: "AI + Design Ideathon",
    description: "Blend AI tools with design thinking to solve campus challenges.",
    domain: "AI",
    link: "https://www.knowafest.com/explore/events?city=Chennai",
    platform: "Knowafest",
    type: "Hackathon",
    tags: ["AI", "Design"],
    date: "Apr 27, 2026",
    location: "Chennai"
  },
  {
    id: 11,
    title: "Product Ops Micro-Internship",
    description: "Learn product analytics, roadmapping, and user research.",
    domain: "Product",
    link: "https://unstop.com/internships",
    platform: "Unstop",
    type: "Internship",
    tags: ["Product", "Startup"],
    date: "May 14, 2026",
    location: "Remote"
  },
  {
    id: 12,
    title: "Design for Social Good",
    description: "Create campaign prototypes for NGO partners.",
    domain: "Design",
    link: "https://unstop.com/competitions",
    platform: "Unstop",
    type: "Event",
    tags: ["Design", "Startup"],
    date: "Jun 18, 2026",
    location: "Online"
  }
];
