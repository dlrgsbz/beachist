import { alerts } from 'firebase-functions/v2';
import { defineSecret } from 'firebase-functions/params';

const githubPat = defineSecret('GITHUB_ACCESS_TOKEN');

export const crashToGithubIssue = alerts.crashlytics.onNewFatalIssuePublished(
  {
    secrets: [githubPat],
  },
  async (event) => {
    const issue = event.data.payload.issue;
    const token = githubPat.value();

    const projectId = process.env.GCLOUD_PROJECT;
    const platform = 'android';

    const issueId = event.data.payload.issue.id;
    const appId = '1:872369417762:android:a9de81fbaac8f66f78a47b';

    const crashlyticsUrl = `https://console.firebase.google.com/project/${projectId}/crashlytics/app/${platform}:${appId}/issues/${issueId}`;

    const issueBody = `
### 🚨 New Crash Detected: ${issue.title}

A new fatal crash was reported by Firebase Crashlytics.

**App Details:**
* **Version:** ${issue.appVersion}
* **Issue ID:** \`${issueId}\`

🔗 **[View Stacktrace and details in Firebase Console](${crashlyticsUrl})**

---
*This issue was generated automatically by Firebase Cloud Functions.*
`;

    const payload = {
      title: `Crash: ${issue.title}`,
      body: issueBody.trim(),
      labels: ['bug', 'crashlytics'],
    };


    try {
      const response = await fetch('https://api.github.com/repos/dlrgsbz/beachist/issues', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
          'Accept': 'application/vnd.github.v3+json',
          'User-Agent': 'Firebase-Crashlytics-Integration',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorData = await response.text();
        console.error(
          `GitHub API Error: ${response.status} ${response.statusText}`,
          errorData
        );
        return;
      }
    } catch (error) {
      console.error('Failed to execute native fetch to GitHub API:', error);
    }
  }
);
