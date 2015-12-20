browserclass = 'Firefox'
language_mapping = {
    'ru_RU': 'ru'
}

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

browser = getattr(webdriver, browserclass)()

def _translate(browser, langname, lines):
    browser.get(
        'https://translate.google.com/#auto/{0}'.format(
            language_mapping[langname]
        )
    )
    type_here = browser.find_element_by_xpath('//*[@id="source"]')
    clear = browser.find_element_by_xpath('//*[@id="gt-clear"]')
    result = browser.find_element_by_xpath('//*[@id="result_box"]')
    newlines = []
    for line in lines:
        newline = line
        print(line)
        if isinstance(line, dict):
            totranslate = list(line.values())[0]
            print(totranslate)
            if '%' not in totranslate and totranslate != '':
                type_here.click()
                type_here.send_keys(totranslate)
                WebDriverWait(browser, 10).until(
                    EC.presence_of_element_located(
                        (By.XPATH, '//*[@id="result_box"]/span')
                    )
                )
                translated = result.text
                newline = {list(line.keys())[0]: translated}
                clear.click()
        newlines.append(newline)
    return newlines


def _configureifylines(lines):
    newlines = []
    for line in lines:
        if line.endswith('\n'):
            line = line[:-1]
        if not line.startswith('#') and line != '':
            liner = line.split('=', 1)
            dummy = liner[1] if len(liner) == 2 else ''
            line = {liner[0]: liner[1]}
        else:
            line = line
        newlines.append(line)
    return newlines


def _unconfigureifylines(lines):
    newlines = []
    for line in lines:
        newline = line
        if isinstance(line, dict):
            newline = "{0}={1}".format(list(line.keys())[0], list(line.values())[0])
        newlines.append(newline)
    return newlines


def translate(lang, newlang):
    filename = lang + '.lang'
    newfilename = newlang + '.lang'
    f = open(filename, 'r')
    lines = f.readlines()
    f.close()
    newlines = _configureifylines(lines)
    import time
    time.sleep(5)
    translated = _translate(browser, newlang, newlines)
    write_lines = _unconfigureifylines(translated)
    f = open(newfilename, 'w')
    f.write('\n'.join(write_lines))
    f.close()
