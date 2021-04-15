/*!
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import { And, Eq, Exists, Ge, Gt, In, Le, Like, Lt, Ne, Not, Or } from '../../src/options/filter.options';

describe('Filters', () => {

  const prop = 'aProperty';
  const numberValue = 7;
  const stringValue = 'foobar';
  const boolValue = false;


  it('builds an eq', () => {
    let eq = Eq(prop, numberValue);
    expect(eq.toString()).toEqual(`eq(${prop},${numberValue})`);

    eq = Eq(prop, stringValue);
    expect(eq.toString()).toEqual(`eq(${prop},"${stringValue}")`);

    eq = Eq(prop, boolValue);
    expect(eq.toString()).toEqual(`eq(${prop},${boolValue})`);
  });


  it('builds an ne', () => {
    let ne = Ne(prop, numberValue);
    expect(ne.toString()).toEqual(`ne(${prop},${numberValue})`);

    ne = Ne(prop, stringValue);
    expect(ne.toString()).toEqual(`ne(${prop},"${stringValue}")`);

    ne = Ne(prop, boolValue);
    expect(ne.toString()).toEqual(`ne(${prop},${boolValue})`);
  });

  it('builds a gt', () => {
    let gt = Gt(prop, numberValue);
    expect(gt.toString()).toEqual(`gt(${prop},${numberValue})`);

    gt = Gt(prop, stringValue);
    expect(gt.toString()).toEqual(`gt(${prop},"${stringValue}")`);

    gt = Gt(prop, boolValue);
    expect(gt.toString()).toEqual(`gt(${prop},${boolValue})`);
  });

  it('builds a ge', () => {
    let ge = Ge(prop, numberValue);
    expect(ge.toString()).toEqual(`ge(${prop},${numberValue})`);

    ge = Ge(prop, stringValue);
    expect(ge.toString()).toEqual(`ge(${prop},"${stringValue}")`);

    ge = Ge(prop, boolValue);
    expect(ge.toString()).toEqual(`ge(${prop},${boolValue})`);

  });
  it('builds a lt', () => {
    let lt = Lt(prop, numberValue);
    expect(lt.toString()).toEqual(`lt(${prop},${numberValue})`);

    lt = Lt(prop, stringValue);
    expect(lt.toString()).toEqual(`lt(${prop},"${stringValue}")`);

    lt = Lt(prop, boolValue);
    expect(lt.toString()).toEqual(`lt(${prop},${boolValue})`);
  });

  it('builds a le', () => {
    let le = Le(prop, numberValue);
    expect(le.toString()).toEqual(`le(${prop},${numberValue})`);

    le = Le(prop, stringValue);
    expect(le.toString()).toEqual(`le(${prop},"${stringValue}")`);

    le = Le(prop, boolValue);
    expect(le.toString()).toEqual(`le(${prop},${boolValue})`);
  });

  it('builds an in', () => {
    const inT = In(prop, numberValue, boolValue, stringValue, 'anotherOne');
    expect(inT.toString()).toEqual(`in(${prop},${numberValue},${boolValue},"${stringValue}","anotherOne")`);
  });

  it('builds a like', () => {
    const like = Like(prop, '*test*');
    expect(like.toString()).toEqual(`like(${prop},"*test*")`);
  });

  it('builds an exists', () => {
    const exists = Exists(prop);
    expect(exists.toString()).toEqual(`exists(${prop})`);
  });

  it('builds an and', () => {
    const and = And(Lt(prop, 7), Gt(prop, 5));
    expect(and.toString()).toEqual(`and(lt(${prop},7),gt(${prop},5))`);
  });

  it('builds an or', () => {
    const or = Or(Lt(prop, 7), Gt(prop, 5));
    expect(or.toString()).toEqual(`or(lt(${prop},7),gt(${prop},5))`);
  });

  it('builds a not', () => {
    let not = Not(Lt(prop, 7));
    expect(not.toString()).toEqual(`not(lt(${prop},7))`);

    not = Not(Eq(prop, stringValue));
    expect(not.toString()).toEqual(`not(eq(${prop},"${stringValue}"))`);

    not = Not(Eq(prop, boolValue));
    expect(not.toString()).toEqual(`not(eq(${prop},${boolValue}))`);
  });

  it('builds big filters', () => {
    const filter = And(
      Or(
        Ne('Prop1', 9),
        Gt('Prop2', 93)
      ),
      Or(
        Like('Prop3', '*exp'),
        And(
          In('Prop4', 'Option1', 'Option2'),
          Le('Prop4', 'Option3')
        )
      ),
      Not(
        Eq('Prop5', 'Option4')
      )
    );
    expect(filter.toString())
      // tslint:disable-next-line:max-line-length
      .toEqual(
        'and(or(ne(Prop1,9),gt(Prop2,93)),or(like(Prop3,"*exp"),and(in(Prop4,"Option1","Option2"),le(Prop4,"Option3"))),not(eq(Prop5,"Option4")))');
  });
});
