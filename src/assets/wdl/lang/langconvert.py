browserclass = 'Firefox'
language_mapping = {
    'ru_RU': 'ru'
}

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


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
        if isinstance(line, list):
            totranslate = line[1]
            if ('%' not in totranslate and
                '§' not in totranslate and
                (totranslate != '' or line[0] == "wdl.translatorCredit")):
                print("Translating {0}".format(line[0]))
                type_here.click()
                type_here.send_keys(totranslate)
                if "wdl.translatorCredit" in line[0]:
                    translated = "Automatically Translated to %s by Google Translate"
                else:
                    WebDriverWait(browser, 10).until(
                        EC.presence_of_element_located(
                            (By.XPATH, '//*[@id="result_box"]/span')
                        )
                    )
                    translated = result.text
                    clear.click()
                newline = [line[0], translated]
        newlines.append(newline)
    return newlines


def _configureifylines(lines):
    newlines = []
    for line in lines:
        if line.endswith('\n'):
            line = line[:-1]
        if not line.startswith('#') and line != '':
            liner = line.split('=', 1)
            if len(liner) == 1:
                liner.append('')
            line = liner
        else:
            line = line
        newlines.append(line)
    return newlines


def _unconfigureifylines(lines):
    newlines = []
    for line in lines:
        newline = line
        if isinstance(line, list):
            newline = "=".join(line)
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
    f.write("{0}\n".format('\n'.join(write_lines)))
    f.close()

def main():
    global browser
    browser = getattr(webdriver, browserclass)()
    i = input("Please type in the language you want to translate to:\n")
    translate('en_US', i)
    browser.quit()

if __name__ == '__main__':
    main()
