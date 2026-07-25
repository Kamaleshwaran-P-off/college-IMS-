import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export default function AppDetails() {
  return (
    <div className="space-y-6">
      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardDescription>React Application Details</CardDescription>
          <CardTitle>Smart Campus AI Platform</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          Centralized overview of the project setup and deployed modules.
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Frontend Stack</CardTitle>
            <CardDescription>UI + animations</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-sm text-muted-foreground">
            <p>React + Vite + TypeScript</p>
            <p>Tailwind CSS + Framer Motion</p>
            <p>Chart.js analytics + modular dashboard layout</p>
          </CardContent>
        </Card>
        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Backend Stack</CardTitle>
            <CardDescription>Services + APIs</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-sm text-muted-foreground">
            <p>Spring Boot 3 + Java 17</p>
            <p>JWT authentication + role guards</p>
            <p>MySQL + JPA</p>
          </CardContent>
        </Card>
        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Modules Enabled</CardTitle>
            <CardDescription>Active capabilities</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-sm text-muted-foreground">
            <p>Role-based dashboards</p>
            <p>Mentor & leave approvals</p>
            <p>Broadcast notifications + carousel</p>
          </CardContent>
        </Card>
        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Admin Controls</CardTitle>
            <CardDescription>Governance</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-sm text-muted-foreground">
            <p>Academic calendar uploads</p>
            <p>Timetable uploads</p>
            <p>Faculty-class assignments</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
