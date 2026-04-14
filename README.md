# Scarfbench Submissions

This repository is the submission intake for [Scarfbench](https://scarfbench.info), a benchmark for evaluating automated code-conversion systems across Java application frameworks. Contributors submit evaluation artifacts by opening a pull request; a continuous integration workflow validates the submission and records its result on the leaderboard.

## Prerequisites

Contributors are expected to have produced evaluation artifacts locally using the `scarf` command-line tool, following the procedure described at [https://scarfbench.info/quickstart/](https://scarfbench.info/quickstart/). Each submission comprises a set of conversion directories, where every directory corresponds to a single benchmark 3-tuple (layer, source framework, target framework) and contains one or more `run_N` subdirectories. 

If run correctly, each run directory would contain a `metadata.json` file. Ensure the folder structure matches [https://scarfbench.info/quickstart/#after-the-run](https://scarfbench.info/quickstart/#after-the-run)

Note: The validation workflow discovers conversion roots by locating every ancestor directory containing one or more `run_*/metadata.json` descendants, and is therefore agnostic to the surrounding structure.

## Submission Procedure

The following steps describe the end-to-end process for submitting an evaluation to Scarfbench.

### 1. Fork the Repository

Create a personal fork of [`scarfbench/submit`](https://github.com/scarfbench/submit). The fork will inherit the default branch `main`, which contains the workflow definitions and documentation only.

### 2. Add Conversion Artifacts

Clone the fork locally and add the conversion directories to the working tree. Contributors are encouraged to commit directly to the fork's `main` branch, though any branch may be used. The commit need not preserve any particular top-level layout, provided that each conversion root contains the expected `run_*/metadata.json` and `run_*/validation/run.log` files.

```bash
git clone https://github.com/<your-username>/submit.git
cd submit
# Copy conversion artifacts into the working tree, then:
git add .
git commit -m "Add submission: <brief descriptor>"
git push origin main
```

### 3. Open a Pull Request against the `submission` Branch

The critical requirement is that the pull request **target** the upstream `submission` branch, not `main`. GitHub's pull-request creation interface defaults the base branch to the upstream default (`main`); contributors must manually change the base branch selector to `submission` before submitting. Failure to do so will result in the validation workflow not being triggered.

A pre-filled comparison URL of the following form may be used to reach the pull-request creation interface with the correct base branch already selected:

```
https://github.com/scarfbench/submit/compare/submission...<your-username>:submit:main?expand=1
```

The pull request description should identify the agent, model, and any relevant variant or configuration details that are not already encoded in the submission's metadata.

### 4. Await Validation

Upon pull-request opening, the continuous integration workflow executes automatically. First-time contributors to the repository are subject to GitHub's standard approval gate; subsequent submissions from the same contributor proceed without manual intervention. The workflow performs the following operations:

1. Enumerates all conversion roots in the submission.
2. Distributes the roots across parallel validation shards, each of which invokes `scarf validate` on its assigned subset.
3. Aggregates the per-shard results into one or more leaderboard JSON files conforming to the Scarfbench leaderboard schema.
4. Posts a confirmation comment on the pull request and closes it upon successful completion.

Validation outputs — including per-run logs, updated metadata, and the generated leaderboard JSON — are retained as workflow artifacts attached to the pull request's check run. These artifacts may be downloaded from the GitHub Actions interface for inspection.

Should validation fail, the pull request is left open and a diagnostic comment is posted. The contributor may then push corrective commits to the source branch; each push re-triggers the workflow.

## Disposition of Submissions

Pull requests are closed rather than merged. The submission's content is preserved in the closed pull request's diff, which remains accessible indefinitely through the GitHub web interface. The `submission` branch is not mutated by the acceptance process and serves solely as a routing target for the workflow's branch filter.

The leaderboard JSON produced by a successful validation is the canonical record of the submission's outcome. Publication of these results to [scarfbench.info](https://scarfbench.info) is performed out-of-band by the maintainers.

## Contact

For questions regarding the submission process or the benchmark methodology, please open an issue in this repository or consult the project homepage at [scarfbench.info](https://scarfbench.info).
