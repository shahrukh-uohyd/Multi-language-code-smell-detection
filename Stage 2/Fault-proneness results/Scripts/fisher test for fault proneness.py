import pandas as pd
import scipy.stats as stats
import numpy as np

def fisher_test_with_adjustment(a, b, c, d, confidence_level=0.95, alternative='greater'):
    """
    Computes Fisher's Exact Test, Odds Ratio, p-value, and Confidence Interval with Haldane-Anscombe correction.

    Args:
        a, b, c, d: Cell counts of 2x2 contingency table.
        confidence_level: CI level (default = 95%).
        alternative: 'greater' (default), 'less', or 'two-sided'.

    Returns:
        Tuple: (odds_ratio, p_value, ci_low, ci_high)
    """
    # Apply Haldane-Anscombe correction if any value is zero
    if 0 in [a, b, c, d]:
        a, b, c, d = a + 0.5, b + 0.5, c + 0.5, d + 0.5

    # Construct contingency table
    table = [[a, b], [c, d]]

    # Compute Odds Ratio
    try:
        odds_ratio = (a * d) / (b * c)
    except ZeroDivisionError:
        odds_ratio = np.inf if a * d > 0 else 0

    # Compute one-sided Fisher's Exact Test p-value
    try:
        _, p_value = stats.fisher_exact(table, alternative=alternative)
    except:
        p_value = np.nan

    # Compute Confidence Interval using log method
    try:
        log_or = np.log(odds_ratio)
        se_log_or = np.sqrt(1/a + 1/b + 1/c + 1/d)
        z = stats.norm.ppf(1 - (1 - confidence_level) / 2)
        ci_low = np.exp(log_or - z * se_log_or)
        ci_high = np.exp(log_or + z * se_log_or)
    except:
        ci_low, ci_high = np.nan, np.nan

    # Handle extreme OR edge cases
    if odds_ratio == 0:
        ci_low, ci_high = 0, np.inf
    elif np.isinf(odds_ratio):
        ci_low, ci_high = 0, np.inf

    return odds_ratio, p_value, (ci_low, ci_high)


def process_csv(file_path, output_path=None, alternative='greater'):
    """
    Processes a CSV file and applies Fisher's test on each row.

    Args:
        file_path: Path to input CSV.
        output_path: Path to save output CSV (optional).
        alternative: One-sided alternative hypothesis ('greater' or 'less').

    Returns:
        DataFrame with test results.
    """
    df = pd.read_csv(file_path)

    results = []

    for index, row in df.iterrows():
        project_name = row.get("Project Name", f"Project_{index + 1}")

        # Extract and safely convert contingency table values
        a = pd.to_numeric(row.get("Both Smelly & Faulty"), errors='coerce') or 0
        b = pd.to_numeric(row.get("Smelly But Not Faulty"), errors='coerce') or 0
        c = pd.to_numeric(row.get("Faulty But Not Smelly"), errors='coerce') or 0
        d = pd.to_numeric(row.get("Not Smelly & Not Faulty"), errors='coerce') or 0

        if (a + b == 0) or (c + d == 0):
            odds_ratio, p_value, ci = None, None, (None, None)
        else:
            odds_ratio, p_value, ci = fisher_test_with_adjustment(a, b, c, d, alternative=alternative)

        results.append([
            project_name, int(a), int(b), int(c), int(d),
            odds_ratio, p_value, ci[0], ci[1]
        ])

    result_df = pd.DataFrame(results, columns=[
        "Project", "a", "b", "c", "d", 
        "Odds Ratio", "p-value", "CI Lower", "CI Upper"
    ])

    if output_path:
        result_df.to_csv(output_path, index=False)
        print(f"Results saved to {output_path}")

    return result_df


# Example usage
if __name__ == "__main__":
    csv_file = "buggy_smelly/abidi/fisher_data_for_fault_analysis.csv"   #path to csv file which contains the contingency table for fisher test
    output_file = "buggy_smelly/abidi/fisher_results_for_fault_analysis.csv"  # path to outpue

    df_result = process_csv(csv_file, output_file, alternative='two-sided')
    print(df_result)
