"""Plot bench/results.txt (output of Benchmark.java) -> docs/results.png"""
import re, sys
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

src = sys.argv[1] if len(sys.argv) > 1 else "bench/results.txt"
out = sys.argv[2] if len(sys.argv) > 2 else "docs/results.png"
txt = open(src).read()
perft = [(int(d), int(n), ok == "OK") for d, n, ok in re.findall(r"perft (\d+) (\d+) (\S+)", txt)]
arena = [(m[0], int(m[1]), int(m[2]), int(m[3]), int(m[4]), float(m[5])) for m in
         re.findall(r"arena (\w+) games (\d+) wins (\d+) draws (\d+) losses (\d+) ms_per_move ([\d.]+)", txt)]
REF = [1, 4, 12, 56, 244, 1396, 8200, 55092, 390216, 3005288]

plt.rcParams.update({"font.family": "DejaVu Sans", "font.size": 11})
fig, (a, b) = plt.subplots(1, 2, figsize=(11, 4.2), gridspec_kw={"width_ratios": [1.15, 1]})

d = [p[0] for p in perft]
a.plot(d, [REF[i] for i in d], "-", color="#9CA3AF", lw=6, alpha=.5, label="published reference")
a.plot(d, [p[1] for p in perft], "o", color="#1F4E9A", ms=7, label="this engine")
a.set_yscale("log"); a.set_xlabel("search depth (plies)"); a.set_ylabel("positions (perft)")
a.set_title(f"Move generator: {sum(p[2] for p in perft)}/{len(perft)} depths exact", loc="left", fontweight="bold")
a.legend(frameon=False, loc="upper left"); a.grid(alpha=.25)
for s in ("top", "right"): a.spines[s].set_visible(False)

names = [f"vs {x[0]}" for x in arena]
w = [100 * x[2] / x[1] for x in arena]; dr = [100 * x[3] / x[1] for x in arena]; l = [100 * x[4] / x[1] for x in arena]
y = range(len(arena))
b.barh(y, w, color="#1F4E9A", label="win"); b.barh(y, dr, left=w, color="#9CA3AF", label="draw")
b.barh(y, l, left=[i + j for i, j in zip(w, dr)], color="#C2410C", label="loss")
for i, x in enumerate(arena):
    b.text(w[i] / 2, i, f"{w[i]:.0f} % wins", color="white", va="center", ha="center", fontweight="bold")
b.set_yticks(list(y)); b.set_yticklabels(names); b.set_xlim(0, 100); b.invert_yaxis()
b.set_xlabel(f"share of {arena[0][1]} games (alternating colours)")
b.set_title("Negamax AI (depth 6) vs baselines", loc="left", fontweight="bold")
b.legend(frameon=False, ncol=3, loc="lower center", bbox_to_anchor=(.5, -.42))
for s in ("top", "right"): b.spines[s].set_visible(False)
fig.tight_layout(); fig.savefig(out, dpi=160, bbox_inches="tight")
print("saved", out)
